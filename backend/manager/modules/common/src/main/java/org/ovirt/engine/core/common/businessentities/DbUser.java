package org.ovirt.engine.core.common.businessentities;

import javax.validation.constraints.Size;

import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class DbUser extends IVdcQueryable {
    private static final long serialVersionUID = 7052102138405696755L;

    /**
     * This is the identifier assigned by the engine to this user for internal
     * use only.
     */
    private Guid id;

    /**
     * This is the identifier assigned by the external directory to this user.
     */
    private ExternalId externalId;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_DOMAIN_SIZE)
    private String domain;

    @Size(min = 1, max = BusinessEntitiesDefinitions.USER_LOGIN_NAME_SIZE)
    private String loginName;

    @Size(max = BusinessEntitiesDefinitions.USER_FIRST_NAME_SIZE)
    private String firstName;

    @Size(max = BusinessEntitiesDefinitions.USER_LAST_NAME_SIZE)
    private String lastName;

    @Size(max = BusinessEntitiesDefinitions.USER_DEPARTMENT_SIZE)
    private String department;

    @Size(max = BusinessEntitiesDefinitions.USER_ROLE_SIZE)
    private String role;

    @Size(max = BusinessEntitiesDefinitions.USER_EMAIL_SIZE)
    private String email;

    @Size(max = BusinessEntitiesDefinitions.USER_NOTE_SIZE)
    private String note;

    /**
     * This flag indicates if the user was available in the directory the last time that it was checked, so {@code true}
     * means it was available and {@code false} means it wasn't.
     */
    private boolean active;

    /**
     * GUI flag only. Do not use for internal logic. The sole purpose of
     * calculating this field is for the GUI user to understand who is admin in
     * a snap on the user grid.
     */
    private boolean isAdmin;


    /**
     * Comma delimited list of group names.
     */
    @Size(min = 1, max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String groupNames;

    /**
     * Comma delimited list of group identifiers.
     */
    @Size(max = BusinessEntitiesDefinitions.USER_GROUP_IDS_SIZE)
    private String groupIds;

    public DbUser() {
        loginName = "";
        firstName = "";
        lastName = "";
        department = "";
        groupNames = "";
        groupIds = "";
        role = "";
        note = "";
    }

    public DbUser(LdapUser ldapUser) {
        externalId = ldapUser.getUserId();
        domain = ldapUser.getDomainControler();
        loginName = ldapUser.getUserName();
        firstName = ldapUser.getName();
        lastName = ldapUser.getSurName();
        department = ldapUser.getDepartment();
        email = ldapUser.getEmail();
        active = true;
        groupNames = ldapUser.getGroup();
        role = "";
        note = "";
    }

    public DbUser(DirectoryUser directoryUser) {
        externalId = directoryUser.getId();
        domain = directoryUser.getDirectoryName();
        loginName = directoryUser.getName();
        firstName = directoryUser.getFirstName();
        lastName = directoryUser.getLastName();
        department = directoryUser.getDepartment();
        email = directoryUser.getEmail();
        active = true;
        role = "";
        note = "";

        StringBuilder namesBuffer = new StringBuilder();
        boolean first = true;
        for (DirectoryGroup directoryGroup : directoryUser.getGroups()) {
            if (!first) {
                namesBuffer.append(",");
            }
            namesBuffer.append(directoryGroup.getName());
            first = false;
        }
        groupNames = namesBuffer.toString();
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public void setExternalId(ExternalId externalId) {
        this.externalId = externalId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String value) {
        domain = value;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String value) {
        loginName = value;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        firstName = value;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String value) {
        lastName = value;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String value) {
        department = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(String value) {
        groupNames = value;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String value) {
        note = value;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String value) {
        role = value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean value) {
        active = value;
    }

    public boolean isGroup() {
        return loginName == null || loginName.trim().isEmpty();
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public void setAdmin(boolean value) {
        isAdmin = value;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setGroupIds(String groupIds) {
        this.groupIds = groupIds;
    }

    public String getGroupIds() {
        return groupIds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
        result = prime * result + ((department == null) ? 0 : department.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((groupNames == null) ? 0 : groupNames.hashCode());
        result = prime * result + (isAdmin ? 1231 : 1237);
        result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + (active ? 1231 : 1237);
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((loginName == null) ? 0 : loginName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DbUser other = (DbUser) obj;
        return (ObjectUtils.objectsEqual(id, other.id)
                && ObjectUtils.objectsEqual(externalId, other.externalId)
                && ObjectUtils.objectsEqual(department, other.department)
                && ObjectUtils.objectsEqual(domain, other.domain)
                && ObjectUtils.objectsEqual(email, other.email)
                && ObjectUtils.objectsEqual(groupNames, other.groupNames)
                && isAdmin == other.isAdmin
                && ObjectUtils.objectsEqual(firstName, other.firstName)
                && ObjectUtils.objectsEqual(note, other.note)
                && ObjectUtils.objectsEqual(role, other.role)
                && active == other.active
                && ObjectUtils.objectsEqual(lastName, other.lastName)
                && ObjectUtils.objectsEqual(loginName, other.loginName));
    }
}
