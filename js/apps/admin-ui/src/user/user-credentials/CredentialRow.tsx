import { ReactNode, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Td } from "@patternfly/react-table";
import {
  Button,
  Dropdown,
  DropdownItem,
  DropdownList,
  MenuToggle,
  Modal,
  ModalVariant,
  ClipboardCopy,
  Alert,
} from "@patternfly/react-core";
import type CredentialRepresentation from "@keycloak/keycloak-admin-client/lib/defs/credentialRepresentation";
import useToggle from "../../utils/useToggle";
import useLocaleSort from "../../utils/useLocaleSort";
import { CredentialDataDialog } from "./CredentialDataDialog";
import useFormatDate from "../../utils/useFormatDate";
import { EllipsisVIcon } from "@patternfly/react-icons";
import { useAdminClient } from "../../admin-client";

type CredentialRowProps = {
  credential: CredentialRepresentation;
  resetPassword: () => void;
  toggleDelete: () => void;
  children: ReactNode;
  userId: string;
};

export const CredentialRow = ({
  credential,
  resetPassword,
  toggleDelete,
  children,
  userId,
}: CredentialRowProps) => {
  const { adminClient } = useAdminClient();
  const formatDate = useFormatDate();
  const { t } = useTranslation();
  const [showData, toggleShow] = useToggle();
  const [kebabOpen, toggleKebab] = useToggle();
  const localeSort = useLocaleSort();
  const [showExportPassword, setShowExportPassword] = useState(false);
  const [exportPassword, setExportPassword] = useState("");
  const [exportError, setExportError] = useState("");

  const rows = useMemo(() => {
    if (!credential.credentialData) {
      return [];
    }

    const credentialData: Record<string, unknown> = JSON.parse(
      credential.credentialData,
    );
    return localeSort(Object.entries(credentialData), ([key]) => key).map<
      [string, string]
    >(([key, value]) => {
      if (typeof value === "string") {
        return [key, value];
      }

      return [key, JSON.stringify(value)];
    });
  }, [credential.credentialData]);

  const viewExportPassword = async () => {
    try {
      setExportError("");
      const result = await adminClient.users.getEncryptedPassword({ id: userId });
      setExportPassword(result.encryptedPassword);
      setShowExportPassword(true);
    } catch {
      setExportError(t("exportPasswordNotFound"));
      setShowExportPassword(true);
    }
  };

  return (
    <>
      {showData && Object.keys(credential).length !== 0 && (
        <CredentialDataDialog
          title={credential.userLabel || t("passwordDataTitle")}
          credentialData={rows}
          onClose={() => {
            toggleShow();
          }}
        />
      )}

      {showExportPassword && (
        <Modal
          variant={ModalVariant.small}
          title={t("exportPasswordTitle")}
          isOpen
          onClose={() => {
            setShowExportPassword(false);
            setExportPassword("");
            setExportError("");
          }}
        >
          {exportError ? (
            <Alert variant="warning" isInline title={exportError} />
          ) : (
            <ClipboardCopy
              isReadOnly
              hoverTip={t("copy")}
              clickTip={t("copied")}
            >
              {exportPassword}
            </ClipboardCopy>
          )}
        </Modal>
      )}

      <Td>{children}</Td>
      <Td>{formatDate(new Date(credential.createdDate!))}</Td>
      <Td>
        <Button
          className="kc-showData-btn"
          variant="link"
          data-testid="showDataBtn"
          onClick={toggleShow}
        >
          {t("showDataBtn")}
        </Button>
      </Td>
      {credential.type === "password" ? (
        <Td isActionCell>
          <Button
            variant="secondary"
            data-testid="resetPasswordBtn"
            onClick={resetPassword}
          >
            {t("resetPasswordBtn")}
          </Button>{" "}
          <Button
            variant="tertiary"
            data-testid="viewExportPasswordBtn"
            onClick={viewExportPassword}
          >
            {t("viewExportPasswordBtn")}
          </Button>
        </Td>
      ) : (
        <Td />
      )}
      <Td isActionCell>
        <Dropdown
          popperProps={{
            position: "right",
          }}
          onOpenChange={toggleKebab}
          toggle={(ref) => (
            <MenuToggle
              ref={ref}
              isExpanded={kebabOpen}
              onClick={toggleKebab}
              variant="plain"
              aria-label="Kebab toggle"
            >
              <EllipsisVIcon />
            </MenuToggle>
          )}
          isOpen={kebabOpen}
        >
          <DropdownList>
            <DropdownItem
              key={credential.id}
              data-testid="deleteDropdownItem"
              component="button"
              onClick={() => {
                toggleDelete();
                toggleKebab();
              }}
            >
              {t("deleteBtn")}
            </DropdownItem>
          </DropdownList>
        </Dropdown>
      </Td>
    </>
  );
};
