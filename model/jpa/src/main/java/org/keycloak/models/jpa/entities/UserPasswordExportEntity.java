/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.models.jpa.entities;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@NamedQueries({
        @NamedQuery(name = "findPasswordExportByUser", query = "select p from UserPasswordExportEntity p where p.userId = :userId and p.realmId = :realmId"),
        @NamedQuery(name = "deletePasswordExportByUser", query = "delete from UserPasswordExportEntity p where p.userId = :userId and p.realmId = :realmId"),
        @NamedQuery(name = "deletePasswordExportByRealm", query = "delete from UserPasswordExportEntity p where p.realmId = :realmId")
})
@Table(name = "USER_PASSWORD_EXPORT")
@Entity
public class UserPasswordExportEntity {

    @Id
    @Column(name = "ID", length = 36)
    @Access(AccessType.PROPERTY)
    private String id;

    @Column(name = "USER_ID", nullable = false, length = 36)
    private String userId;

    @Column(name = "REALM_ID", nullable = false, length = 36)
    private String realmId;

    @Column(name = "ENCRYPTED_PASSWORD", nullable = false, length = 2048)
    private String encryptedPassword;

    @Column(name = "CREATED_DATE")
    private Long createdDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof UserPasswordExportEntity)) return false;
        UserPasswordExportEntity that = (UserPasswordExportEntity) o;
        return id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
