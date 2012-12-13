
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACT_INSETACTION" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL
)
AS
   v_sysPrefix VARCHAR2(256);
   v_statusFieldId NUMBER(10,0);
   v_statusFieldName VARCHAR2(256);
   v_actionId NUMBER(10,0);
   v_oldStatusId NUMBER(10,0);
   v_closedStatusId NUMBER(10,0);
   v_oldStatus VARCHAR2(128);
   v_headerDescription VARCHAR2(4096);
BEGIN
   /*
 * Get the Prefix of the business area.
 */
   SELECT sys_prefix
     INTO v_sysPrefix
     FROM business_areas
      WHERE sys_id = v_systemId;

   /*
 * Get the id and the display name of status field in this business area.
 */
   SELECT field_id,
              display_name
     INTO v_statusFieldId,
          v_statusFieldName
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'status_id';

   /*
 * Get the Id of the status closed in this business area.
 */
   SELECT NVL(TYPE_ID, 0)
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = v_statusFieldId
              AND NAME = 'closed';

   /*
 * Get the id of the old status.
 */
   SELECT status_id
     INTO v_oldStatusId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   /*
 * Get the name of the old status
 */
   SELECT display_name
     INTO v_oldStatus
     FROM types t
      WHERE sys_id = v_systemId
              AND field_id = v_statusFieldId
              AND TYPE_ID = v_oldStatusId;

   /*
 * Get the max_request_id
 */
   SELECT max_action_id
     INTO v_actionId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   /*
 * Prepare the header description.
 */
   IF ( v_oldStatusId <> v_closedStatusId ) THEN
   BEGIN
      v_headerDescription := ' [ ' || v_statusFieldName || ' changed from ''' || v_oldStatus || ''' to ''Closed'' ] ' || CHR(10);

   END;
   ELSE
   BEGIN
      v_headerDescription := '';

   END;
   END IF;

   v_headerDescription := v_headerDescription || ' [ Transfer to ' || v_sysPrefix || '# pending... ] ' || CHR(10);

   /*
 * Increment the action id by 1.
 */
   v_actionId := v_actionId + 1;

   UPDATE requests
      SET status_id = v_closedStatusId,
          USER_ID = v_userId,
          max_action_id = v_actionId,
          lastupdated_datetime = SYS_EXTRACT_UTC(SYSTIMESTAMP),
          append_interface = 101,
          notify = 1,
          notify_loggers = 1,
          replied_to_action = 0
      WHERE sys_id = v_systemId
     AND request_id = v_requestId;

   INSERT INTO actions
     ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action )
     ( SELECT sys_id,
              request_id,
              v_actionId,
              category_id,
              v_closedStatusId,
              severity_id,
              request_type_id,
              SUBJECT,
              '',
              is_private,
              parent_request_id,
              v_userId,
              due_datetime,
              logged_datetime,
              SYS_EXTRACT_UTC(SYSTIMESTAMP),
              v_headerDescription,
              '',
              '',
              '',
              101,
              1,
              1,
              0
       FROM actions
          WHERE sys_id = v_systemId
                  AND request_id = v_requestId
                  AND action_id = v_actionId - 1 );

   INSERT INTO action_users
     ( SELECT sys_id,
              request_id,
              v_actionId,
              user_type_id,
              USER_ID,
              ordering,
              is_primary
       FROM action_users
          WHERE sys_id = v_systemId
                  AND request_id = v_requestId
                  AND action_id = v_actionId - 1 );

   INSERT INTO actions_ex
     ( SELECT sys_id,
              request_id,
              v_actionId,
              field_id,
              bit_value,
              datetime_value,
              int_value,
              real_value,
              varchar_value,
              text_value,
              type_value
       FROM actions_ex
          WHERE sys_id = v_systemId
                  AND request_id = v_requestId
                  AND action_id = v_actionId - 1 );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_GETACTIONTEXT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             action_id,
             DESCRIPTION,
             header_description,
             attachments
        FROM actions
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_GETALLACTIONS" 
(
  v_systemId IN number DEFAULT NULL ,
  v_requestId IN number DEFAULT NULL ,
  v_sortOrder IN VARCHAR2 DEFAULT NULL
)
AS
   v_sort VARCHAR2(10);
   v_query VARCHAR2(7999);
BEGIN
   v_query := '
           SELECT
           sys_id   ,
           request_id   ,
           action_id   ,
           category_id   ,
           status_id   ,
           severity_id   ,
           request_type_id   ,
           subject  ,
           description ,
           is_private   ,
           parent_request_id   ,
           user_id   ,
           due_datetime,
           logged_datetime,
           lastupdated_datetime,
           header_description,
           attachments,
           summary,
           '''' as "memo",
           append_interface   ,
           notify   ,
           notify_loggers   ,
           replied_to_action,
           office_id
           FROM
                   actions
          WHERE
                sys_id = '  ||to_char(v_systemId,'nls_language IN VARCHAR2') || ' and
                request_id=' ||to_char(v_requestId,'nls_language IN VARCHAR2')|| '
           order by action_id ' ||v_sortOrder || '
        ';
   

  EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_GETDIFFACTIONS" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_request_id IN NUMBER DEFAULT NULL ,
  v_replied_to_action IN NUMBER DEFAULT NULL ,
  v_max_action_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             action_id,
             category_id,
             status_id,
             severity_id,
             request_type_id,
             SUBJECT,
             '''' "description",
             is_private,
             parent_request_id,
             USER_ID,
             due_datetime,
             logged_datetime,
             lastupdated_datetime,
             '''' "header_description",
             '''' "attachments",
             summary,
             '''''''' "memo",
             0 "append_interface",
             0 "notify",
             0 "notify_loggers",
             0 "replied_to_action",
             office_id
        FROM actions
         WHERE sys_id = v_sys_id
                 AND request_id = v_request_id
                 AND action_id >= v_replied_to_action
                 AND action_id <= v_max_action_id
        ORDER BY action_id DESC;

   OPEN cv_2 FOR
      SELECT *
        FROM action_users
         WHERE sys_id = v_sys_id
                 AND request_id = v_request_id
                 AND action_id >= v_replied_to_action
                 AND action_id <= v_max_action_id;

   OPEN cv_3 FOR
      SELECT *
        FROM actions_ex
         WHERE sys_id = v_sys_id
                 AND request_id = v_request_id
                 AND action_id >= v_replied_to_action
                 AND action_id <= v_max_action_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_GETUPDATEDREQUESTS" 
(
  v_since IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT a.sys_id "sys_id",
                      a.request_id "request_id",
                      ba.sys_prefix "sys_prefix"
        FROM actions a
               JOIN business_areas ba
                ON a.sys_id = ba.sys_id
         WHERE lastupdated_datetime >= v_since;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_LOOKUPBYID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             action_id,
             category_id,
             status_id,
             severity_id,
             request_type_id,
             SUBJECT,
             DESCRIPTION,
             is_private,
             parent_request_id,
             USER_ID,
             due_datetime,
             logged_datetime,
             lastupdated_datetime,
             NVL(header_description, '') "header_description",
             NVL(attachments, '') "attachments",
             NVL(summary, '') "summary",
             NVL(memo, '') "memo",
             append_interface,
             notify,
             notify_loggers,
             replied_to_action,
             office_id
        FROM actions act
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND action_id = v_actionId;

   OPEN cv_2 FOR
      SELECT *
        FROM action_users
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_UPDATEACTIONTEXT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_description IN CLOB DEFAULT NULL ,
  v_headerDesc IN CLOB DEFAULT NULL ,
  v_attachments IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE actions
      SET DESCRIPTION = v_description,
          header_description = v_headerDesc,
          attachments = v_attachments
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_UPDATEATTACHMENTS" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_attachments IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE actions
      SET attachments = v_attachments
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ACTION_UPTRANSFERINFO" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_targetRequest IN VARCHAR2 DEFAULT NULL
)
AS
   v_actionId NUMBER(10,0);
   v_startIndex NUMBER(10,0);
   v_endIndex NUMBER(10,0);
   v_description VARCHAR2(4096);
   v_headerDescription VARCHAR2(7999);
BEGIN
   v_description := '[ Request transferred to ' || v_targetRequest || ' ]';

   -- Get the max_request_id
   SELECT max_action_id
     INTO v_actionId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   v_headerDescription := '';

  SELECT to_clob(header_description)
     INTO v_headerDescription
     FROM actions
      WHERE sys_id = v_systemId
             AND request_id = v_requestId
              AND action_id = v_actionId;

   v_startIndex := INSTR(v_headerDescription, '[ Transfer');

   v_endIndex := INSTR(v_headerDescription, '# pending... ]');

   IF ( ( v_startIndex >= 0 )
     AND ( v_endIndex > 0 )
     AND ( v_endIndex > v_startIndex ) ) THEN
   BEGIN
      v_headerDescription := REPLACE(v_headerDescription, SUBSTR(v_headerDescription, v_startIndex, v_endIndex + 14), v_description);

   END;
   ELSE
   BEGIN
      v_headerDescription := v_headerDescription || v_description;

   END;
   END IF;

   -- Update the requests table.
   UPDATE requests
      SET header_description = v_headerDescription
      WHERE sys_id = v_systemId
     AND request_id = v_requestId;

   -- Update the actions table.
   UPDATE actions
      SET header_description = v_headerDescription
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_DELETE_ROLES_USERS" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_role_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE roles_users

      WHERE sys_id = v_sys_id
              AND role_id = v_role_id
              AND USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETALLDATATYPES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM datatypes ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETBUSAREASBYUSERID" 
(
  v_user_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_superUserId NUMBER(10,0);
BEGIN
   SELECT USER_ID
     INTO v_superUserId
     FROM super_users
      WHERE USER_ID = v_user_id;

   IF ( v_superUserId IS NOT NULL ) THEN
   BEGIN
      OPEN cv_1 FOR
         SELECT *
           FROM business_areas ba;

   END;
   ELSE
   BEGIN
      OPEN cv_1 FOR
         SELECT *
           FROM business_areas
            WHERE sys_id IN ( SELECT ba.sys_id
                              FROM business_areas ba
                                     JOIN roles_users ru
                                      ON ba.sys_id = ru.sys_id
                                 WHERE ru.role_id IN ( 8,9,10 )
                                         AND ru.USER_ID = v_user_id
                                         AND ru.is_active = 1 );

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETRBYSIDUSERID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM roles_users
         WHERE sys_id = v_sys_id
                 AND USER_ID = v_user_id
        ORDER BY role_id;

   OPEN cv_2 FOR
      SELECT *
        FROM super_users
         WHERE USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETROLEPERBYSYSID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_role_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT f.NAME,
                      NVL(rp.sys_id, v_sys_id) "sys_id",
                      NVL(rp.role_id, v_role_id) "role_id",
                      NVL(rp.field_id, f.field_id) "field_id",
                      NVL(rp.gpermissions, 0) "permission",
                      NVL(rp.dpermissions, 0) "dpermission"
        FROM fields f
               LEFT JOIN roles_permissions rp
                ON f.sys_id = rp.sys_id
               AND rp.role_id = v_role_id
               AND f.field_id = rp.field_id
         WHERE f.sys_id = v_sys_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETROLESBYSYSID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM roles
         WHERE sys_id = v_sys_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETUSERFORBA" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_field_id NUMBER(10,0);
BEGIN
   SELECT field_id
     INTO v_field_id
     FROM fields
      WHERE NAME = 'category_id';

   OPEN cv_1 FOR
      SELECT *
        FROM type_users
         WHERE sys_id = v_sys_id
                 AND field_id = v_field_id
                 AND USER_ID = v_user_id
        ORDER BY TYPE_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_GETUSERID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT r.role_id,
             r.rolename,
             NVL(ru.USER_ID, -1) "user_id"
        FROM roles r
               LEFT JOIN roles_users ru
                ON r.sys_id = ru.sys_id
               AND r.role_id = ru.role_id
               AND ru.USER_ID = v_user_id
               AND ru.is_active = 1
         WHERE r.sys_id = v_sys_id
                 AND r.role_id > 6
        ORDER BY r.role_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_INSERT_ROLES_USERS" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_role_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO roles_users
     ( sys_id, role_id, USER_ID, is_active )
     VALUES ( v_sys_id, v_role_id, v_user_id, v_is_active );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_ROLE_LUPBYSIDRNAME" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_rolename IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM roles
         WHERE sys_id = v_sys_id
                 AND rolename = v_rolename;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADMIN_SET_USER_PASSWORD" 
(
  v_userLogin IN VARCHAR2 DEFAULT NULL ,
  v_password IN VARCHAR2 DEFAULT NULL
)
AS
   v_already_defined NUMBER;
BEGIN
   v_already_defined := 0;

   SELECT COUNT(*)
     INTO v_already_defined
     FROM user_passwords
      WHERE user_login = v_userLogin;

   IF v_already_defined = 0 THEN
   BEGIN
      INSERT INTO user_passwords
        ( user_login, PASSWORD )
        VALUES ( v_userLogin, v_password );

   END;
   ELSE
   BEGIN
      UPDATE user_passwords
         SET PASSWORD = v_password
         WHERE user_login = v_userLogin;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADSYNC_DEACTIVATEUSER" 
(
  v_userId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   /*
 * Mark the user record as inactive.
 */
   UPDATE users
      SET is_active = 0
      WHERE USER_ID = v_userId;

   /*
 * Mark the corresponding type user records as inactive.
 */
   UPDATE type_users
      SET is_active = 0
      WHERE USER_ID = v_userId;

   /*
 * Mark the corresponding roles_users records as inactive
 */
   UPDATE roles_users
      SET is_active = 0
      WHERE USER_ID = v_userId;

   /*
 * Mark the corresponding BAUser records as inactive
 */
   UPDATE business_area_users
      SET is_active = 0
      WHERE USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADSYNC_GETEXCLUDEDENTRIES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT u.user_login
        FROM exclusion_list el
               JOIN users u
                ON el.USER_ID = u.USER_ID
         WHERE el.sys_id = 0
                 AND el.user_type_id = -1
        ORDER BY el.user_type_id,
                 user_login;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADSYNC_INSERTUSER" 
(
  v_user_id OUT NUMBER,
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_first_name IN NVARCHAR2 DEFAULT NULL ,
  v_last_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_web_config IN CLOB DEFAULT NULL ,
  v_windows_config IN CLOB DEFAULT NULL ,
  v_is_on_vacation IN NUMBER DEFAULT NULL ,
  v_is_display IN NUMBER DEFAULT NULL ,
  v_cn IN VARCHAR2 DEFAULT NULL ,
  v_distinguished_name IN VARCHAR2 DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_member_of IN CLOB DEFAULT NULL ,
  v_member IN CLOB DEFAULT NULL ,
  v_mail_nickname IN VARCHAR2 DEFAULT NULL ,
  v_location IN VARCHAR2 DEFAULT NULL ,
  v_extension IN VARCHAR2 DEFAULT NULL ,
  v_mobile IN VARCHAR2 DEFAULT NULL ,
  v_home_phone IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN
   SELECT NVL(MAX(USER_ID), 0) + 1
     INTO v_user_id
     FROM users
      WHERE USER_ID < 50000;

   INSERT INTO users
     ( USER_ID, user_login, first_name, last_name, display_name, email, is_active, user_type_id, web_config, windows_config, is_on_vacation, is_display, cn, distinguished_name, NAME, member_of, member, mail_nickname, location, extension, mobile, home_phone )
     VALUES ( v_user_id, v_user_login, v_first_name, v_last_name, v_display_name, v_email, v_is_active, v_user_type_id, v_web_config, v_windows_config, v_is_on_vacation, v_is_display, v_cn, v_distinguished_name, v_name, v_member_of, v_member, v_mail_nickname, v_location, v_extension, v_mobile, v_home_phone );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ADSYNC_UPDATEUSER" 
(
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_first_name IN NVARCHAR2 DEFAULT NULL ,
  v_last_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_web_config IN CLOB DEFAULT NULL ,
  v_windows_config IN CLOB DEFAULT NULL ,
  v_is_on_vacation IN NUMBER DEFAULT NULL ,
  v_is_display IN NUMBER DEFAULT NULL ,
  v_cn IN VARCHAR2 DEFAULT NULL ,
  v_distinguished_name IN VARCHAR2 DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_member_of IN CLOB DEFAULT NULL ,
  v_member IN CLOB DEFAULT NULL ,
  v_mail_nickname IN VARCHAR2 DEFAULT NULL ,
  v_location IN VARCHAR2 DEFAULT NULL ,
  v_extension IN VARCHAR2 DEFAULT NULL ,
  v_mobile IN VARCHAR2 DEFAULT NULL ,
  v_home_phone IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN
   UPDATE users
      SET user_login = v_user_login,
          first_name = v_first_name,
          last_name = v_last_name,
          display_name = v_display_name,
          email = v_email,
          is_active = v_is_active,
          user_type_id = v_user_type_id,
          web_config = v_web_config,
          windows_config = v_windows_config,
          is_on_vacation = v_is_on_vacation,
          is_display = v_is_display,
          cn = v_cn,
          distinguished_name = v_distinguished_name,
          NAME = v_name,
          member_of = v_member_of,
          member = v_member,
          mail_nickname = v_mail_nickname,
          location = v_location,
          extension = v_extension,
          mobile = v_mobile,
          home_phone = v_home_phone
      WHERE USER_ID = v_user_id;

   /*
 * Mark the corresponding ba_user record as active.
 */
   UPDATE business_area_users
      SET is_active = v_is_active
      WHERE USER_ID = v_user_id;

   /*
 * Mark the corresponding type user records as active.
 */
   UPDATE type_users
      SET is_active = v_is_active
      WHERE USER_ID = v_user_id;

   /*
 * Mark the corresponding role user records as active
 */
   UPDATE roles_users
      SET is_active = v_is_active
      WHERE USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_FORMS_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_form_id IN NUMBER DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_title IN VARCHAR2 DEFAULT NULL ,
  v_shortname IN VARCHAR2 DEFAULT NULL ,
  v_form_config IN CLOB DEFAULT NULL
)
AS
BEGIN
   INSERT INTO ba_forms
     ( sys_id, form_id, NAME, title, shortname, form_config )
     VALUES ( v_sys_id, v_form_id, v_name, v_title, v_shortname, v_form_config );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_FORMS_LOOKANDNAME" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             form_id,
             NAME,
             title,
             shortname,
             form_config
        FROM ba_forms
         WHERE sys_id = v_systemId
                 AND ( NAME = v_name
                 OR shortname = v_name );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_FORMS_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_form_id IN NUMBER DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_title IN VARCHAR2 DEFAULT NULL ,
  v_shortname IN VARCHAR2 DEFAULT NULL ,
  v_form_config IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE ba_forms
      SET NAME = v_name,
          title = v_title,
          shortname = v_shortname,
          form_config = v_form_config
      WHERE sys_id = v_sys_id
     AND form_id = v_form_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_GETALLBUSINESSAREAS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_areas ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_GETANALYSTBUSINESSAREAS" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_userTypeId NUMBER(10,0);
BEGIN
   SELECT user_type_id
     INTO v_userTypeId
     FROM user_types ut
      WHERE ut.NAME = 'Assignee';

   OPEN cv_1 FOR
      SELECT *
        FROM business_areas
         WHERE sys_id IN ( SELECT tu.sys_id
                           FROM type_users tu
                              WHERE tu.USER_ID = v_userId
                                      AND tu.user_type_id = v_userTypeId )
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_GETBUSAREASBYUSERID" 
/*CREATE OR REPLACE PROCEDURE stp_ba_getBusinessAreasByUserId*/
(
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_baFieldId NUMBER(10,0);
   v_ipFieldId NUMBER(10,0);
BEGIN
   SELECT field_id
     INTO v_baFieldId
     FROM fields
      WHERE NAME = 'sys_id';

   SELECT field_id
     INTO v_ipFieldId
     FROM fields
      WHERE NAME = 'is_private';

   OPEN cv_1 FOR
      /*
 * Script that lists out the Business area visible to a list of roles specified.
 */
      SELECT *
        FROM business_areas
         WHERE sys_id IN ( SELECT ba.sys_id
                           FROM business_areas ba
                                  JOIN roles_permissions rp-- This is for checking the user/logger permission on sys_id field.
                                  
                                   ON rp.sys_id = ba.sys_id
                                  JOIN roles_permissions rpp-- This is for checking the user/logger permission on is_private field.
                                  
                                   ON rp.sys_id = rpp.sys_id
                                  AND rp.field_id = v_baFieldId
                                  AND rpp.field_id = v_ipFieldId
                                  LEFT JOIN roles_users ru-- This is for considering the user's BA specific roles.
                                  
                                   ON rp.sys_id = ru.sys_id
                                  AND ru.USER_ID = v_userId
                                  AND ru.is_active = 1
                                  LEFT JOIN roles_permissions rpba-- This is for checking the BA's Specific Role's permission on sys_id field.
                                  
                                   ON rpba.sys_id = ru.sys_id
                                  AND rpba.role_id = ru.role_id
                                  AND rpba.field_id = v_baFieldId
                                  LEFT JOIN roles_permissions rpip-- This is for checking the BA's Specific Role's permission on is_private field.
                                  
                                   ON rpip.sys_id = ru.sys_id
                                  AND rpip.role_id = ru.role_id
                                  AND rpip.field_id = v_ipFieldId
                              WHERE ba.is_active = 1
                                      AND rp.role_id IN ( 1 )
                                      AND rpp.role_id IN ( 1 )
                                      -- If BA is normal, then VIEW permission is required on sys_id
                                      -- by virtue either of User/Logger role OR BA Role the user is associated with.
                                      AND ( ( ba.is_private = 0
                                      AND ( (rp.gpermissions) <> 0
                                      OR (rpba.gpermissions) <> 0 ) )
                                      -- If BA is Private, then VIEW permission is required on sys_id along with VIEW permission on private field
                                      -- by virtue either of User/Logger role OR of BA Role the user is associated with.
                                      OR ( ba.is_private = 1
                                      AND ( ( (rp.gpermissions) <> 0
                                      OR (rpba.gpermissions) <> 0 )
                                      AND ( (rpp.gpermissions) <> 0
                                      OR (rpip.gpermissions) <> 0 ) ) ) ) )
        ORDER BY display_name;

END

;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_LOOKUPBYEMAIL" 
(
  v_email IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_areas
         WHERE email = v_email
                 OR--- For those BAs with a single email id.
                  email LIKE '%' || v_email || '%';--- For those BAs with multiple email ids.
                 

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_LOOKUPBYNAME" 
(
  v_name IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_areas
         WHERE NAME = v_name;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_LOOKUPBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_areas
         WHERE sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_LOOKUPBYSYSTEMPREFIX" 
(
  v_systemPrefix IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_areas
         WHERE sys_prefix = v_systemPrefix;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BA_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_sys_prefix IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_type IN NVARCHAR2 DEFAULT NULL ,
  v_location IN NVARCHAR2 DEFAULT NULL ,
  v_date_created IN DATE DEFAULT NULL ,
  v_max_request_id IN NUMBER DEFAULT NULL ,
  v_max_email_actions IN NUMBER DEFAULT NULL ,
  v_is_email_active IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_sys_config IN CLOB DEFAULT NULL ,
  v_field_config IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE business_areas
      SET NAME = v_name,
          display_name = v_display_name,
          email = v_email,
          sys_prefix = v_sys_prefix,
          DESCRIPTION = v_description,
          TYPE = v_type,
          location = v_location,
          max_email_actions = v_max_email_actions,
          is_email_active = v_is_email_active,
          is_active = v_is_active,
          date_created = v_date_created,
          is_private = v_is_private,
          sys_config = v_sys_config,
          field_config = v_field_config
      WHERE sys_id = v_sys_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BARULES_GETALLBARULES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM ba_rules ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BARULES_LOOKUPIDANDRULEID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM ba_rules
         WHERE sys_id = v_systemId
        ORDER BY sequence_no;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BAUSER_GETALLBAUSERS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_area_users ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BAUSER_GETUSERBYBA" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             USER_ID
        FROM business_area_users
         WHERE sys_id = v_systemId
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BAUSER_LOOKUPBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM business_area_users
         WHERE sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BUSINESS_AREA_USERS_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE post_process_rules

      WHERE sys_id = v_sys_id
              AND USER_ID = v_user_id;

   DELETE roles_users

      WHERE sys_id = v_sys_id
              AND USER_ID = v_user_id;

   DELETE type_users

      WHERE sys_id = v_sys_id
              AND USER_ID = v_user_id;

   DELETE business_area_users

      WHERE sys_id = v_sys_id
              AND USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_BUSINESS_AREA_USERS_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
   v_max NUMBER(10,0);
BEGIN
   INSERT INTO business_area_users
     ( sys_id, USER_ID, is_active )
     VALUES ( v_sys_id, v_user_id, v_is_active );

   SELECT NVL(MAX(rule_id), 0)
     INTO v_max
     FROM post_process_rules ;

   INSERT INTO post_process_rules
     ( rule_id, USER_ID, sys_id, priority, xml_string, DESCRIPTION, ENABLED )
     VALUES ( v_max + 1, v_user_id, v_sys_id, 1, '<?xml version="1.0" encoding="utf-8" ?>
   <Rule id="1">
        <expressions>
             <expression id="1">
                  <name>max_action_id</name>
                  <op>EQ</op>
                  <value>1</value>
             </expression>
        </expressions>
        <actions>
             <action>
                  <name>sms_id</name>
                  <op>SET</op>
                  <value>1</value>
             </action>
        </actions>
   </Rule>', 'Add Request', 1 );

   INSERT INTO post_process_rules
     ( rule_id, USER_ID, sys_id, priority, xml_string, DESCRIPTION, ENABLED )
     VALUES ( v_max + 2, v_user_id, v_sys_id, 1, '<?xml version="1.0" encoding="utf-8" ?>
   <Rule id="1">
        <expressions>
             <expression id="1">
                  <name>max_action_id</name>
                  <op>NE</op>
                  <value>1</value>
             </expression>
        </expressions>
        <actions>
             <action>
                  <name>sms_id</name>
                  <op>SET</op>
                  <value>2</value>
             </action>
        </actions>
   </Rule>', 'Update Request', 1 );

   stp_admin_insert_roles_users(v_sys_id,
                                12,
                                v_user_id,
                                1);

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_CUST_REQ_CLOYEARLY" 
(
  v_sysPrefix IN VARCHAR2 DEFAULT NULL ,
  v_year IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   CURSOR mycursor
     IS SELECT DISTINCT request_id,
   action_id
     FROM tt_actionTmp a
      WHERE a.sys_id = v_systemId
     AND EXTRACT(YEAR FROM a.lastupdated_datetime) = v_year
     AND a.status_id = v_closedStatusId
     ORDER BY request_id,
   action_id ASC;
   v_systemId NUMBER(10,0);
   v_closedStatusId NUMBER(10,0);
   v_requestId NUMBER(10,0);
   v_actionId NUMBER(10,0);
BEGIN
   SELECT sys_id
     INTO v_systemId
     FROM business_areas
      WHERE sys_prefix = v_sysPrefix;

   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   DELETE FROM tt_reqTmp;

   INSERT INTO tt_reqTmp (
      -- get request ids closed and not appended after @start  INTO ___ #reqTmp__ or logged after @end
      SELECT sys_id,
                  request_id
        FROM requests
         WHERE sys_id = v_systemId
                 AND ( ( EXTRACT(YEAR FROM lastupdated_datetime) < v_year
                 AND status_id = v_closedStatusId )
                 OR EXTRACT(YEAR FROM logged_datetime) > v_year ) );

   DELETE FROM tt_actionTmp;

   INSERT INTO tt_actionTmp (
      -- get actions for requests not in #tmp  INTO ___#actionTmp__
      SELECT a.sys_id,
                  a.request_id,
                  a.action_id,
                  a.status_id,
                  a.category_id,
                  a.request_type_id,
                  a.severity_id,
                  a.USER_ID,
                  a.logged_datetime,
                  a.lastupdated_datetime
        FROM actions a
         WHERE a.sys_id = v_systemId
                 AND a.request_id NOT IN ( --DELETE FROM tt_actionTmp;

                                           --INSERT INTO tt_actionTmp (
                                           SELECT request_id
                                           FROM tt_reqTmp   ) );

   OPEN myCursor;

   FETCH myCursor INTO v_requestId,v_actionId;

   WHILE ( sqlserver_utilities.fetch_status(myCursor%FOUND) = 0)
            
   LOOP
      BEGIN
         IF ( v_actionId = 1 ) THEN
         BEGIN
            INSERT INTO tt_tmpc
              VALUES ( v_systemId, v_requestId, v_actionId );

         END;
         ELSE
         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT status_id
                          FROM actions
                             WHERE sys_id = v_systemId
                                     AND request_id = v_requestId
                                     AND action_id = v_actionId - 1 ) != v_closedStatusId );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               INSERT INTO tt_tmpc
                 VALUES ( v_systemId, v_requestId, v_actionId );

            END;
            END IF;

         END;
         END IF;

         FETCH myCursor INTO v_requestId,v_actionId;

      END;
   END LOOP;

   CLOSE myCursor;

   DELETE FROM tt_tmp_action;

   INSERT INTO tt_tmp_action (
      SELECT a.systemId,
             a.requestId,
             a.actionId,
             1 / CAST(COUNT(*) AS FLOAT(53)) "weight"
        FROM tt_tmp a
               LEFT JOIN action_users au
                ON a.systemId = au.sys_id
               AND a.requestId = au.request_id
               AND a.actionId = au.action_id
               AND ( au.user_type_id IS NULL
               OR au.user_type_id = 3 )
         WHERE a.systemId = v_systemId
        GROUP BY a.systemId,a.requestId,a.actionId );

   OPEN cv_1 FOR
      SELECT NVL(u.user_login, '-') "user_login",
             sqlserver_utilities.str(SUM(weight), 20, 2) "count"
        FROM tt_tmp_action t
               LEFT JOIN action_users au
                ON au.sys_id = t.systemId
               AND au.request_id = t.requestId
               AND au.action_id = t.actionId
               AND ( au.user_type_id IS NULL
               OR au.user_type_id = 3 )
               LEFT JOIN users u
                ON u.USER_ID = au.USER_ID
        GROUP BY u.user_login
        ORDER BY u.user_login;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpc ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp_action ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_reqTmp ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_actionTmp ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DACTION_INSERT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_dActionLog IN CLOB DEFAULT NULL
)
AS
BEGIN
   INSERT INTO daction_log
     ( sys_id, request_id, action_id, daction_log )
     VALUES ( v_systemId, v_requestId, v_actionId, v_dActionLog );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DBSYNC_INSERTUSER" 
(
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_first_name IN NVARCHAR2 DEFAULT NULL ,
  v_last_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_web_config IN CLOB DEFAULT NULL ,
  v_windows_config IN CLOB DEFAULT NULL ,
  v_is_on_vacation IN NUMBER DEFAULT NULL ,
  v_is_display IN NUMBER DEFAULT NULL ,
  v_cn IN NVARCHAR2 DEFAULT NULL ,
  v_distinguished_name IN NVARCHAR2 DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_member_of IN CLOB DEFAULT NULL ,
  v_member IN CLOB DEFAULT NULL ,
  v_mail_nickname IN NVARCHAR2 DEFAULT NULL ,
  v_location IN VARCHAR2 DEFAULT NULL ,
  v_extension IN VARCHAR2 DEFAULT NULL ,
  v_mobile IN VARCHAR2 DEFAULT NULL ,
  v_home_phone IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN
   INSERT INTO users
     ( USER_ID, user_login, first_name, last_name, display_name, email, is_active, user_type_id, web_config, windows_config, is_on_vacation, is_display, cn, distinguished_name, NAME, member_of, member, mail_nickname, location, extension, mobile, home_phone )
     VALUES ( v_user_id, v_user_login, v_first_name, v_last_name, v_display_name, v_email, v_is_active, v_user_type_id, v_web_config, v_windows_config, v_is_on_vacation, v_is_display, v_cn, v_distinguished_name, v_name, v_member_of, v_member, v_mail_nickname, v_location, v_extension, v_mobile, v_home_phone );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DEPEN_GETDPENDENCIES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             dep_id,
             dep_name,
             dep_level,
             dep_type,
             dep_config
        FROM dependencies ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DEPENDES_GAPPSDEPENDENCIES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT D.sys_id,
             D.dep_id,
             D.dep_name,
             D.dep_level,
             D.dep_type,
             D.dep_config
        FROM dependencies D
               JOIN ( SELECT DISTINCT D.sys_id,
                                      D.dep_id
                      FROM fields f
                             JOIN dependent_fields df
                              ON f.sys_id = df.sys_id
                             AND f.field_id = df.field_id
                             JOIN dependencies D
                              ON df.sys_id = D.sys_id
                             AND df.dep_id = D.dep_id
                         WHERE f.data_type_id = 9
                                 AND D.dep_level = 'APP_DEPENDENCY' ) temp
                ON D.sys_id = temp.sys_id
               AND D.dep_id = temp.dep_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DEPFIELD_GETALDTFIELDS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM dependent_fields ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DISPLAY_GROUP_DELETE" 
(
  -- Add the parameters for the stored procedure here
  v_id IN NUMBER DEFAULT NULL ,
  v_display_name IN VARCHAR2 DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_ret_value OUT NUMBER
)
AS
BEGIN
   DELETE display_groups

      WHERE id = v_id;

   v_ret_value := 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DISPLAY_GROUP_INSERT" 
(
  v_id IN NUMBER DEFAULT NULL ,
  v_display_name IN VARCHAR2 DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_new_id OUT NUMBER
)
AS
BEGIN
   INSERT INTO display_groups
     ( display_name, display_order, is_active )
     VALUES ( v_display_name, v_display_order, v_is_active );

   v_new_id := sqlserver_utilities.identity;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DISPLAY_GROUP_LOOKUPALL" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
-- Add the parameters for the stored procedure here
BEGIN
   OPEN cv_1 FOR
      SELECT id,
             display_name,
             display_order,
             is_active
        FROM display_groups ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DISPLAY_GROUP_UPDATE" 
(
  v_id IN NUMBER DEFAULT NULL ,
  v_display_name IN VARCHAR2 DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE display_groups
      SET display_name = v_display_name,
          display_order = v_display_order,
          is_active = v_is_active
      WHERE id = v_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_DTF_GETALLDATETIMEFORMATS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM datetime_formats ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_EL_GETCOMPLIST" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM exclusion_list ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ER_GETALLEXTERNALRESOURCES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT resource_id,
             resource_name,
             resource_def
        FROM external_resources ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ER_LOOKUPBYID" 
(
  v_resource_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT resource_id,
             resource_name,
             resource_def
        FROM external_resources
         WHERE resource_id = v_resource_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ER_LOOKUPBYNAME" 
(
  v_resource_name IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT resource_id,
             resource_name,
             resource_def
        FROM external_resources
         WHERE resource_name = v_resource_name;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_GETALLDESCRIPTORS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT 0 "sys_id",
                      f.field_id "field_id",
                      f.NAME "name",
                      f.display_name "display_name",
                      f.DESCRIPTION "description",
                      f.data_type_id "data_type_id",
                      f.is_active "is_active",
                      f.is_extended "is_extended",
                      f.is_private "is_private",
                      f.tracking_option "tracking_option",
                      f.permission "permission",
                      f.regex "regex",
                      f.is_dependent,
                      f.display_order "display_order",
                      f.display_group "display_group",
                      fd.field_descriptor "field_descriptor"
        FROM fields f
               JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
         WHERE f.is_active = 1
        ORDER BY f.field_id,
                 f.NAME;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_GETALLFIELDDESCRIPTORS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT f.sys_id,
             f.field_id,
             NVL(fd.field_descriptor, f.NAME) "field_descriptor",
             NVL(fd.is_primary, 0) "is_primary"
        FROM fields f
               LEFT JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
        ORDER BY f.sys_id,
                 f.field_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_GETDBSFIELDID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT f.sys_id,
             f.field_id,
             NVL(fd.field_descriptor, f.NAME) "field_descriptor",
             NVL(fd.is_primary, 0) "is_primary"
        FROM fields f
               LEFT JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
         WHERE f.sys_id = v_sys_id
                 AND f.field_id = v_field_id
        ORDER BY f.sys_id,
                 f.field_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_GETDESCRIPTORTABLE" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT f.*,
             fd.field_descriptor
        FROM fields f
               LEFT JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
         WHERE f.sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_GETPDBYFIELDID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM field_descriptors
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND is_primary = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_LBYSYSIDFIELDDR" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldDesc IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT f.*
        FROM fields f
               LEFT JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
         WHERE f.sys_id = v_systemId
                 AND ( fd.field_descriptor = v_fieldDesc
                 OR ( f.NAME = v_fieldDesc
                 OR f.display_name = v_fieldDesc ) );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FD_LOOKUPBYSYSTEMID" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT f.sys_id,
             f.field_id,
             NVL(fd.field_descriptor, f.NAME) "field_descriptor",
             NVL(fd.is_primary, 0) "is_primary"
        FROM fields f
               LEFT JOIN field_descriptors fd
                ON f.sys_id = fd.sys_id
               AND f.field_id = fd.field_id
         WHERE fd.sys_id = v_sysId
        ORDER BY f.sys_id,
                 f.field_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_data_type_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_extended IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_tracking_option IN NUMBER DEFAULT NULL ,
  v_permission IN NUMBER DEFAULT NULL ,
  v_regex IN VARCHAR2 DEFAULT NULL ,
  v_is_dependent IN NUMBER DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_display_group IN NUMBER DEFAULT NULL ,
  v_returnValue OUT NUMBER
)
AS
   v_delete NUMBER(10,0);
   v_dataTypeId NUMBER(10,0);
   v_temp NUMBER(1, 0) := 0;
BEGIN
   SELECT datatype_id
     INTO v_dataTypeId
     FROM datatypes
      WHERE NAME = 'type';

   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE NOT EXISTS ( SELECT field_id
                          FROM actions_ex
                             WHERE sys_id = v_sys_id
                                     AND field_id = v_field_id );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   IF v_temp = 1 THEN
   BEGIN
      DELETE fields

         WHERE sys_id = v_sys_id
                 AND field_id = v_field_id;

      DELETE roles_permissions

         WHERE sys_id = v_sys_id
                 AND field_id = v_field_id;

      IF ( v_data_type_id = v_dataTypeId ) THEN
      BEGIN
         DELETE types

            WHERE sys_id = v_sys_id
                    AND field_id = v_field_id;

         DELETE type_users

            WHERE sys_id = v_sys_id
                    AND field_id = v_field_id;

      END;
      END IF;

      v_returnValue := 1;

   END;
   ELSE
   BEGIN
      v_returnValue := 0;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_DESCRIPTOR_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_field_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE field_descriptors

      WHERE sys_id = v_sys_id
              AND field_id = v_field_id
              AND field_descriptor = v_field_descriptor
              AND is_primary = v_is_primary;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_DESCRIPTOR_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_field_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_sys_id, v_field_id, v_field_descriptor, v_is_primary );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_GEFBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM fields
         WHERE sys_id = v_systemId
                 AND is_extended = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_GETALLFIELDS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM fields ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_GETFFSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM fields
         WHERE sys_id = v_systemId
                 AND is_extended = 0;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_GETFIELDSYIDUID" /*stp_field_getFieldsBySystemIdAndUserId*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT f.*
        FROM fields f
               JOIN roles_permissions rp
                ON f.sys_id = rp.sys_id
               AND ( rp.role_id = 1
               OR rp.role_id = 2 )
               AND f.field_id = rp.field_id
               LEFT JOIN roles_users ru
                ON ru.sys_id = f.sys_id
               AND ru.USER_ID = v_userId
               AND ru.is_active = 1
               LEFT JOIN roles_permissions rpp
                ON ru.sys_id = rpp.sys_id
               AND ru.role_id = rpp.role_id
               AND rpp.field_id = f.field_id
         WHERE f.sys_id = v_systemId
                 /*
           * Check if user has permissions by virtue of the user/
     logger role.
           */
                 AND ( ( (rp.gpermissions) <> 0 )
                 /*
           * Check if user has permissions by virtue of his association
     with the BA.
           */
                 OR ( (rpp.gpermissions) <> 0 ) )
        ORDER BY f.field_id;

END
;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_data_type_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_extended IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_tracking_option IN NUMBER DEFAULT NULL ,
  v_permission IN NUMBER DEFAULT NULL ,
  v_regex IN VARCHAR2 DEFAULT NULL ,
  v_is_dependent IN NUMBER DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_display_group IN NUMBER DEFAULT NULL
)
AS
   v_fieldID NUMBER(10,0);
   v_i NUMBER(10,0);
   v_maxRoleId NUMBER(10,0);
BEGIN
   SELECT (NVL(MAX(field_id), 0) + 1)
     INTO v_fieldID
     FROM fields
      WHERE sys_id = v_sys_id;

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group )
     VALUES ( v_sys_id, v_fieldID, v_name, v_display_name, v_description, v_data_type_id, v_is_active, v_is_extended, v_is_private, v_tracking_option, v_permission, v_regex, v_is_dependent, v_display_order, v_display_group );

   v_i := 1;

   SELECT MAX(role_id)
     INTO v_maxRoleId
     FROM roles ;

   WHILE ( v_i < v_maxRoleId )
   LOOP
      BEGIN
         DBMS_OUTPUT.PUT_LINE('INSERT INTO roles_permissions values(' || CAST(v_sys_id AS VARCHAR2) || ',' || CAST(v_i AS VARCHAR2) || ',' || +CAST(v_fieldId AS VARCHAR2) || ' , 4, 0)');

         INSERT INTO roles_permissions
           ( sys_id, role_id, field_id, gpermissions, dpermissions )
           VALUES ( v_sys_id, v_i, v_fieldId, 4, 0 );

         DBMS_OUTPUT.PUT_LINE('FINISHED INSERT');

         v_i := v_i + 1;

      END;
   END LOOP;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_LOOKUPBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM fields
         WHERE sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_LUPBSANDFIELDID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             field_id,
             NAME,
             display_name,
             DESCRIPTION,
             data_type_id,
             is_active,
             is_extended,
             is_private,
             tracking_option,
             permission,
             regex,
             is_dependent,
             display_order,
             display_group
        FROM fields
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_LUPBYSANDFIELDNAME" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM fields
         WHERE sys_id = v_systemId
                 AND NAME = v_fieldName;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_FIELD_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_data_type_id IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_extended IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_tracking_option IN NUMBER DEFAULT NULL ,
  v_permission IN NUMBER DEFAULT NULL ,
  v_regex IN NVARCHAR2 DEFAULT NULL ,
  v_is_dependent IN NUMBER DEFAULT NULL ,
  v_display_order IN NUMBER DEFAULT NULL ,
  v_display_group IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE fields
      SET NAME = v_name,
          display_name = v_display_name,
          DESCRIPTION = v_description,
          data_type_id = v_data_type_id,
          is_active = v_is_active,
          is_extended = v_is_extended,
          is_private = v_is_private,
          tracking_option = v_tracking_option,
          permission = v_permission,
          regex = v_regex,
          is_dependent = v_is_dependent,
          display_order = v_display_order,
          display_group = v_display_group
      WHERE field_id = v_field_id
     AND sys_id = v_sys_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_HL_GETHOLIDAYS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT office,
             to_timestamp( holiday_date,'mm.dd.yyyy hh24:mi:ss'),
             office_zone,
             DESCRIPTION
        FROM holidays_list ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_IS_SUPER_USER" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT COUNT(*) usercount
        FROM super_users
         WHERE USER_ID = v_userId
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_MLU_GETALLMAILLISTUSERS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM mail_list_users ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_MLU_GETMAILINGLISTS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT mail_list_id,
             USER_ID
        FROM mail_list_users
        ORDER BY mail_list_id,
                 USER_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_MLU_GETMEMBERSBYEMAILLIKE" 
(
  v_email IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT mlu.USER_ID
        FROM mail_list_users mlu
               JOIN users uc
                ON mlu.mail_list_id = uc.USER_ID
         WHERE uc.email LIKE v_email || '%'
                 OR uc.user_login LIKE v_email || '%';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_NR_GETALLNOTIFICATIONRULES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM notification_rules ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_NR_LOOKUPBYNRULEID" 
(
  v_ruleId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM notification_rules
         WHERE notification_id = v_ruleId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_R_GRANALYST" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_analystId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
   v_assignee_type_id NUMBER(10,0);
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
BEGIN
   SELECT user_type_id
     INTO v_assignee_type_id
     FROM user_types
      WHERE NAME = 'Assignee';

   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   DELETE FROM tt_tmp;

   INSERT INTO tt_tmp (
      SELECT DISTINCT r.request_id
        FROM requests r
               JOIN request_users ru
                ON r.sys_id = ru.sys_id
               AND r.request_id = ru.request_id
               AND ru.user_type_id = v_assignee_type_id
         WHERE r.sys_id = v_systemId
                 AND ru.USER_ID = v_analystId
                 AND r.status_id != v_closedStatusId );

   OPEN cv_1 FOR
      SELECT req.sys_id,
             req.request_id,
             req.category_id,
             req.status_id,
             req.severity_id,
             req.request_type_id,
             req.SUBJECT,
             req.DESCRIPTION "description",
             req.is_private,
             req.parent_request_id,
             req.USER_ID,
             req.max_action_id,
             req.due_datetime,
             req.logged_datetime,
             req.lastupdated_datetime,
             NVL(req.header_description, '''') "header_description",
             NVL(req.attachments, '''') "attachments",
             NVL(req.summary, '''') "summary",
             NVL(req.memo, '''') "memo",
             req.append_interface,
             req.notify,
             req.notify_loggers,
             req.replied_to_action,
             req.office_id
        FROM requests req
         WHERE req.sys_id = v_systemId
                 AND req.request_id IN ( SELECT *
                                         FROM tt_tmp  )
        ORDER BY req.request_id DESC;

   OPEN cv_2 FOR
      -- Get the Corresponding request-user objects
      SELECT *
        FROM request_users ru
         WHERE ru.sys_id = v_systemId
                 AND ru.request_id IN ( SELECT *
                                        FROM tt_tmp  )
        ORDER BY ru.request_id DESC;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RE_GETREQUESTSBYFIELD" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_start IN DATE DEFAULT NULL ,
  v_end IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR,
  cv_5 IN OUT SYS_REFCURSOR,
  cv_6 IN OUT SYS_REFCURSOR,
  cv_7 IN OUT SYS_REFCURSOR,
  cv_8 IN OUT SYS_REFCURSOR
)
AS
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
BEGIN
   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   DELETE FROM tt_tmpy1;

   INSERT INTO tt_tmpy1 (
      SELECT sys_id,
             request_id,
             MAX(action_id) "action_id"
        FROM actions
         WHERE (sys_id = v_systemId
                 AND lastupdated_datetime < v_start
                 AND request_id NOT IN (                                   
                                      SELECT request_id
                                      FROM requests
                                      WHERE sys_id = v_systemId
                                      AND status_id = v_closedStatusId ) )
        GROUP BY sys_id,request_id );

   DELETE FROM tt_tmpy2;

   INSERT INTO tt_tmpy2 (
      SELECT sys_id,
             request_id,
             MAX(action_id) "action_id"
        FROM actions
         WHERE (sys_id = v_systemId
                 AND lastupdated_datetime < v_end
                 AND request_id NOT IN ( 
                                     SELECT request_id
                                     FROM requests
                                     WHERE sys_id = v_systemId
                                      AND status_id = v_closedStatusId ) )
        GROUP BY sys_id,request_id );

   OPEN cv_1 FOR
      SELECT ((NVL(table2.Category, table1.Category),"Type",
             NVL(StartCount, 0), "Start",
             NVL(EndCount, 0) ,"End",
             (NVL(EndCount, 0), - NVL(StartCount, 0)) "Difference")
        FROM ( SELECT t.display_name "Category",
                      COUNT(*) "StartCount"
               FROM ( SELECT a.sys_id,
                             a.request_id,
                             a.action_id,
                             a.category_id
                      FROM actions a
                             JOIN tt_tmpy1
                              ON a.sys_id = tt_tmpy1.sys_id
                             AND a.request_id = tt_tmpy1.request_id
                             AND a.action_id = tt_tmpy1.action_id
                         WHERE (a.sys_id = v_systemId
                                 AND a.status_id <> v_closedStatusId) ) tmp
                      JOIN types t
                       ON t.sys_id = tmp.sys_id
                      AND t.TYPE_ID = tmp.category_id
                  WHERE (t.sys_id = v_systemId
                          AND t.field_id = 3)
                 GROUP BY t.display_name ) table1
               FULL JOIN ( SELECT t.display_name "Category",
                                  COUNT(*) "EndCount"
                           FROM ( SELECT a.sys_id,
                                         a.request_id,
                                         a.action_id,
                                         a.category_id
                                  FROM actions a
                                         JOIN tt_tmpy2
                                          ON a.sys_id = tt_tmpy2.sys_id
                                         AND a.request_id = tt_tmpy2.request_id
                                         AND a.action_id = tt_tmpy2.action_id
                                     WHERE 9a.sys_id = v_systemId
                                             AND a.status_id <> v_closedStatusId )) tmp
                                  JOIN types t
                                   ON t.sys_id = tmp.sys_id
                                  AND t.TYPE_ID = tmp.category_id
                              WHERE (t.sys_id = v_systemId
                                      AND t.field_id = 3)
                             GROUP BY t.display_name ) table2
                ON table2.Category = table1.Category
        ORDER BY TYPE;

   OPEN cv_2 FOR
      SELECT NVL(table2.STATUS, table1.STATUS) "Type",
             NVL(StartCount, 0) "Start",
             NVL(EndCount, 0) "End",
             (NVL(EndCount, 0) - NVL(StartCount, 0)) "Difference"
        FROM ( SELECT t.display_name "Status",
                      COUNT(*) "StartCount"
               FROM ( SELECT a.sys_id,
                             a.request_id,
                             a.action_id,
                             a.Status_id
                      FROM actions a
                             JOIN tt_tmpy1
                              ON a.sys_id = tt_tmpy1.sys_id
                             AND a.request_id = tt_tmpy1.request_id
                             AND a.action_id = tt_tmpy1.action_id
                         WHERE a.sys_id = v_systemId
                                 AND a.status_id <> v_closedStatusId ) tmp
                      JOIN types t
                       ON t.sys_id = tmp.sys_id
                      AND t.TYPE_ID = tmp.Status_id
                  WHERE t.sys_id = v_systemId
                          AND t.field_id = 4
                 GROUP BY t.display_name ) table1
               FULL JOIN ( SELECT t.display_name "Status",
                                  COUNT(*) "EndCount"
                           FROM ( SELECT a.sys_id,
                                         a.request_id,
                                         a.action_id,
                                         a.Status_id
                                  FROM actions a
                                         JOIN tt_tmpy2
                                          ON a.sys_id = tt_tmpy2.sys_id
                                         AND a.request_id = tt_tmpy2.request_id
                                         AND a.action_id = tt_tmpy2.action_id
                                     WHERE a.sys_id = v_systemId
                                             AND a.status_id <> v_closedStatusId ) tmp
                                  JOIN types t
                                   ON t.sys_id = tmp.sys_id
                                  AND t.TYPE_ID = tmp.Status_id
                              WHERE t.sys_id = v_systemId
                                      AND t.field_id = 4
                             GROUP BY t.display_name ) table2
                ON table2.STATUS = table1.STATUS
        ORDER BY TYPE;

   OPEN cv_3 FOR
      SELECT NVL(table2.Severity, table1.Severity) "Type",
             NVL(StartCount, 0) "Start",
             NVL(EndCount, 0) "End",
             (NVL(EndCount, 0) - NVL(StartCount, 0)) "Difference"
        FROM ( SELECT t.display_name "Severity",
                      COUNT(*) "StartCount"
               FROM ( SELECT a.sys_id,
                             a.request_id,
                             a.action_id,
                             a.Severity_id
                      FROM actions a
                             JOIN tt_tmpy1
                              ON a.sys_id = tt_tmpy1.sys_id
                             AND a.request_id = tt_tmpy1.request_id
                             AND a.action_id = tt_tmpy1.action_id
                         WHERE a.sys_id = v_systemId
                                 AND a.status_id <> v_closedStatusId ) tmp
                      JOIN types t
                       ON t.sys_id = tmp.sys_id
                      AND t.TYPE_ID = tmp.Severity_id
                  WHERE t.sys_id = v_systemId
                          AND t.field_id = 5
                 GROUP BY t.display_name ) table1
               FULL JOIN ( SELECT t.display_name "Severity",
                                  COUNT(*) "EndCount"
                           FROM ( SELECT a.sys_id,
                                         a.request_id,
                                         a.action_id,
                                         a.Severity_id
                                  FROM actions a
                                         JOIN tt_tmpy2
                                          ON a.sys_id = tt_tmpy2.sys_id
                                         AND a.request_id = tt_tmpy2.request_id
                                         AND a.action_id = tt_tmpy2.action_id
                                     WHERE a.sys_id = v_systemId
                                             AND a.status_id <> v_closedStatusId ) tmp
                                  JOIN types t
                                   ON t.sys_id = tmp.sys_id
                                  AND t.TYPE_ID = tmp.Severity_id
                              WHERE t.sys_id = v_systemId
                                      AND t.field_id = 5
                             GROUP BY t.display_name ) table2
                ON table2.Severity = table1.Severity
        ORDER BY TYPE;

   OPEN cv_4 FOR
      SELECT NVL(table2.Request_Type, table1.Request_Type) "Type",
             NVL(StartCount, 0) "Start",
             NVL(EndCount, 0) "End",
             (NVL(EndCount, 0) - NVL(StartCount, 0)) "Difference"
        FROM ( SELECT t.display_name "Request_Type",
                      COUNT(*) "StartCount"
               FROM ( SELECT a.sys_id,
                             a.request_id,
                             a.action_id,
                             a.Request_Type_id
                      FROM actions a
                             JOIN tt_tmpy1
                              ON a.sys_id = tt_tmpy1.sys_id
                             AND a.request_id = tt_tmpy1.request_id
                             AND a.action_id = tt_tmpy1.action_id
                         WHERE a.sys_id = v_systemId
                                 AND a.status_id <> v_closedStatusId ) tmp
                      JOIN types t
                       ON t.sys_id = tmp.sys_id
                      AND t.TYPE_ID = tmp.Request_Type_id
                  WHERE t.sys_id = v_systemId
                          AND t.field_id = 6
                 GROUP BY t.display_name ) table1
               FULL JOIN ( SELECT t.display_name "Request_Type",
                                  COUNT(*) "EndCount"
                           FROM ( SELECT a.sys_id,
                                         a.request_id,
                                         a.action_id,
                                         a.Request_Type_id
                                  FROM actions a
                                         JOIN tt_tmpy2
                                          ON a.sys_id = tt_tmpy2.sys_id
                                         AND a.request_id = tt_tmpy2.request_id
                                         AND a.action_id = tt_tmpy2.action_id
                                     WHERE a.sys_id = v_systemId
                                             AND a.status_id <> v_closedStatusId ) tmp
                                  JOIN types t
                                   ON t.sys_id = tmp.sys_id
                                  AND t.TYPE_ID = tmp.Request_Type_id
                              WHERE t.sys_id = v_systemId
                                      AND t.field_id = 6
                             GROUP BY t.display_name ) table2
                ON table2.Request_Type = table1.Request_Type
        ORDER BY TYPE;

   OPEN cv_5 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the start of the interval
      -----------------------------------------------------------------------------------------------
      SELECT t.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM ( SELECT a.sys_id,
                      a.request_id,
                      a.action_id,
                      a.status_id
               FROM actions a
                      JOIN tt_tmpy1
                       ON tt_tmpy1.sys_id = a.sys_id
                      AND tt_tmpy1.request_id = a.request_id
                      AND tt_tmpy1.action_id = a.action_id
                  WHERE a.sys_id = v_systemId
                          AND a.status_id <> v_closedStatusId ) t
               LEFT JOIN action_users au
                ON au.sys_id = t.sys_id
               AND au.request_id = t.request_id
               AND au.action_id = t.action_id
               AND au.user_type_id = 2
               LEFT JOIN users u
                ON u.USER_ID = au.USER_ID
         WHERE t.sys_id = v_systemId
        ORDER BY t.request_id DESC;

   OPEN cv_6 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the end of the interval
      -----------------------------------------------------------------------------------------------
      SELECT t.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM ( SELECT a.sys_id,
                      a.request_id,
                      a.action_id,
                      a.status_id
               FROM actions a
                      JOIN tt_tmpy2
                       ON a.sys_id = tt_tmpy2.sys_id
                      AND a.request_id = tt_tmpy2.request_id
                      AND a.action_id = tt_tmpy2.action_id
                  WHERE a.sys_id = v_systemId
                          AND a.status_id <> v_closedStatusId ) t
               LEFT JOIN action_users au
                ON au.sys_id = t.sys_id
               AND au.request_id = t.request_id
               AND au.action_id = t.action_id
               AND au.user_type_id = 2
               LEFT JOIN users u
                ON au.USER_ID = u.USER_ID
         WHERE t.sys_id = v_systemId
        ORDER BY t.request_id DESC;

   OPEN cv_7 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the start of the interval
      -----------------------------------------------------------------------------------------------
      SELECT t.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM ( SELECT a.sys_id,
                      a.request_id,
                      a.action_id,
                      a.status_id
               FROM actions a
                      JOIN tt_tmpy1
                       ON tt_tmpy1.sys_id = a.sys_id
                      AND tt_tmpy1.request_id = a.request_id
                      AND tt_tmpy1.action_id = a.action_id
                  WHERE a.sys_id = v_systemId
                          AND a.status_id <> v_closedStatusId ) t
               LEFT JOIN action_users au
                ON au.sys_id = t.sys_id
               AND au.request_id = t.request_id
               AND au.action_id = t.action_id
               AND au.user_type_id = 3
               LEFT JOIN users u
                ON u.USER_ID = au.USER_ID
         WHERE t.sys_id = v_systemId
        ORDER BY t.request_id DESC;

   OPEN cv_8 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the end of the interval
      -----------------------------------------------------------------------------------------------
      SELECT t.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM ( SELECT a.sys_id,
                      a.request_id,
                      a.action_id,
                      a.status_id
               FROM actions a
                      JOIN tt_tmpy2
                       ON a.sys_id = tt_tmpy2.sys_id
                      AND a.request_id = tt_tmpy2.request_id
                      AND a.action_id = tt_tmpy2.action_id
                  WHERE a.sys_id = v_systemId
                          AND a.status_id <> v_closedStatusId ) t
               LEFT JOIN action_users au
                ON au.sys_id = t.sys_id
               AND au.request_id = t.request_id
               AND au.action_id = t.action_id
               AND au.user_type_id = 3
               LEFT JOIN users u
                ON au.USER_ID = u.USER_ID
         WHERE t.sys_id = v_systemId
        ORDER BY t.request_id DESC;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpy1 ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpy2 ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RE_GETURBYUSERROLE" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_start IN DATE DEFAULT NULL ,
  v_end IN DATE DEFAULT NULL ,
  v_userTypeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
BEGIN
   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   OPEN cv_1 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the start of the interval
      -----------------------------------------------------------------------------------------------
      SELECT a.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM requests a
               LEFT JOIN action_users au
                ON au.sys_id = a.sys_id
               AND au.request_id = a.request_id
               AND au.action_id = a.max_action_id
               AND au.user_type_id = v_userTypeId
               LEFT JOIN users u
                ON u.USER_ID = au.USER_ID
         WHERE a.sys_id = v_systemId
                 AND a.sys_id = v_systemId
                 AND a.status_id <> v_closedStatusId
                 AND a.lastupdated_datetime < v_start
        ORDER BY a.request_id DESC;

   OPEN cv_2 FOR
      -----------------------------------------------------------------------------------------------
      -- Get the Data at the end of the interval
      -----------------------------------------------------------------------------------------------
      SELECT a.request_id,
                   REPLACE(NVL(u.display_name, '-'), ',', ' ') "user_login"
        FROM requests a
               LEFT JOIN action_users au
                ON au.sys_id = a.sys_id
               AND au.request_id = a.request_id
               AND au.action_id = a.max_action_id
               AND au.user_type_id = v_userTypeId
               LEFT JOIN users u
                ON au.USER_ID = u.USER_ID
         WHERE a.sys_id = v_systemId
                 AND a.sys_id = v_systemId
                 AND a.status_id <> v_closedStatusId
                 AND a.lastupdated_datetime < v_end
        ORDER BY a.request_id DESC;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RE_REQ_CLO_BETN" /*stp_report_request_closed_between*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_start IN DATE DEFAULT NULL ,
  v_end IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   CURSOR mycursor
     IS SELECT DISTINCT request_id,
   action_id
     FROM tt_actionTmp a
      WHERE a.sys_id = v_systemId
     AND a.lastupdated_datetime >= v_start
     AND a.lastupdated_datetime <= v_end
     AND a.status_id = v_closedStatusId
     ORDER BY request_id,
   action_id ASC;
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --   )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
   v_requestId NUMBER(10,0);
   v_actionId NUMBER(10,0);
BEGIN
   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   DELETE FROM tt_reqTmp;

   INSERT INTO tt_reqTmp (
      -- get request ids closed and not appended after @start  INTO ___ #reqTmp__ or logged after @end
      SELECT sys_id,
                   request_id
        FROM requests
         WHERE sys_id = v_systemId
                 AND ( ( lastupdated_datetime < v_start
                 AND status_id = v_closedStatusId )
                 OR logged_datetime > v_end ) );

   DELETE FROM tt_actionTmp;

   INSERT INTO tt_actionTmp (
      -- get actions for requests not in #tmp  INTO ___#actionTmp__
      SELECT a.sys_id,
                   a.request_id,
                   a.action_id,
                   a.status_id,
                   a.category_id,
                   a.request_type_id,
                   a.severity_id,
                   a.USER_ID,
                   a.logged_datetime,
                   a.lastupdated_datetime
        FROM actions a
         WHERE  a.sys_id = v_systemId
                 AND a.request_id NOT IN (  DELETE FROM tt_actionTmp;

                                           INSERT INTO tt_actionTmp (
                 
                                            SELECT request_id
                                           FROM tt_reqTmp  ) ) );


                                           
                                           
   OPEN myCursor;

   FETCH myCursor INTO v_requestId,v_actionId;

   WHILE ( sqlserver_utilities.fetch_status(myCursor%FOUND) = 0 )
   LOOP
      BEGIN
         IF ( v_actionId = 1 ) THEN
         BEGIN
            INSERT INTO tt_tmp
              VALUES ( v_systemId, v_requestId, v_actionId );

         END;
         ELSE
         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT status_id
                          FROM actions
                             WHERE sys_id = v_systemId
                                     AND request_id = v_requestId
                                     AND action_id = v_actionId - 1 ) != v_closedStatusId );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               INSERT INTO tt_tmp
                 VALUES ( v_systemId, v_requestId, v_actionId );

            END;
            END IF;

         END;
         END IF;

         FETCH myCursor INTO v_requestId,v_actionId;

      END;
   END LOOP;

   CLOSE myCursor;

   OPEN cv_1 FOR
      SELECT u.display_name,
             COUNT(*)
        FROM actions a
               JOIN tt_tmp t
                ON a.sys_id = t.systemId
               AND a.request_id = t.requestId
               AND a.action_id = t.actionId
               JOIN users u
                ON u.USER_ID = a.USER_ID
        GROUP BY u.display_name
        ORDER BY u.display_name;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_reqTmp ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_actionTmp ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_GBO_EOD_REPORT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_date IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      --IF(
      --     @systemId != 106 AND      --GBO
      --     @systemId != 109 AND      --FinopHyd
      --     @systemId != 115 AND     --CashTrack
      --     @systemId != 201 AND     --tbits
      --     @systemId != 228 AND     --GBOTact
      --     @systemId != 105          --GenDev
      --  )
      --BEGIN
      --     SELECT @systemId = -1
      --END
      SELECT r.request_id,
                   r.logged_datetime,
                   r.lastupdated_datetime,
                 sqlserver_utilities.datediff('DD', r.logged_datetime, r.lastupdated_datetime) "pdays",
                   req.display_name "request_type",
                   r.SUBJECT
        FROM requests r
               RIGHT JOIN ( SELECT b.sys_id,
                                   b.request_id
                            FROM actions a
                                   JOIN ( SELECT a.sys_id,
                                                 a.request_id,
                                                 a.status_id,
                                                 MIN(a.action_id) "action_id"
                                          FROM actions a
                                                 JOIN types stat
                                                  ON a.sys_id = stat.sys_id
                                                 AND stat.field_id = 4
                                                 AND a.status_id = stat.TYPE_ID
                                             WHERE a.sys_id = v_systemId
                                                     AND a.logged_datetime < v_date
                                                     AND a.lastupdated_datetime > v_date
                                                     AND stat.NAME = 'closed'
                                            GROUP BY a.sys_id,a.request_id,a.status_id ) b
                                    ON a.sys_id = b.sys_id
                                   AND a.request_id = b.request_id
                                   AND a.action_id = b.action_id - 1
                               WHERE a.sys_id = v_systemId
                                       AND a.status_id <> b.status_id ) a
                ON r.sys_id = a.sys_id
               AND r.request_id = a.request_id
               JOIN types stat
                ON r.sys_id = stat.sys_id
               AND stat.field_id = 4
               AND r.status_id = stat.TYPE_ID
               JOIN types req
                ON r.sys_id = req.sys_id
               AND req.field_id = 6
               AND r.request_type_id = req.TYPE_ID
        ORDER BY r.request_id DESC;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_GBO_GETAUDITINFO" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN VARCHAR2 DEFAULT NULL
)
AS
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tbits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_query VARCHAR2(2000);
BEGIN
   v_query := ' SELECT
        (
        SELECT
             name
        FROM
             types
        WHERE
             sys_id=' ||to_char(v_systemId) || '  AND
             field_id = 4 AND
             type_id = r.status_id
        ) as ''status'',
        (
        SELECT
             user_login
        FROM
             users
        WHERE
             user_id = r.user_id
        ) as ''lastupdatedby'',
        r.lastupdated_datetime as ''lastupdated'',
        r.request_id as ''requestId''
   FROM
        requests r
   WHERE
        r.sys_id =' ||to_char( v_systemId);

   IF ( v_requestId <> 'all' ) THEN
   BEGIN
      v_query := v_query || ' AND r.request_id = ' || v_requestId;

   END;
   END IF;

   v_query := v_query || ' order by r.request_id';

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_GEALISTTOMAIL" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_assignee_type_id NUMBER(10,0);
BEGIN
   SELECT user_type_id
     INTO v_assignee_type_id
     FROM user_types
      WHERE NAME = 'Assignee';

   OPEN cv_1 FOR
      --IF(
      --     @systemId != 106 AND      --GBO
      --     @systemId != 109 AND      --FinopHyd
      --     @systemId != 115 AND     --CashTrack
      --     @systemId != 201 AND     --tbits
      --     @systemId != 228 AND     --GBOTact
      --     @systemId != 105          --GenDev
      --  )
      --BEGIN
      --     SELECT @systemId = -1
      --END
      SELECT DISTINCT USER_ID
        FROM request_users
         WHERE sys_id = v_systemId
                 AND user_type_id = v_assignee_type_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_GETTOTALOPENED" /*CREATE OR REPLACE PROCEDURE stp_report_getTotalOpened*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_start IN DATE DEFAULT NULL ,
  v_end IN DATE DEFAULT NULL ,
   cv_1 IN OUT SYS_REFCURSOR
)
AS
   CURSOR mycursor
     IS SELECT DISTINCT request_id,
   action_id
     FROM actions a
      WHERE a.sys_id = v_systemId
     AND a.logged_datetime >= v_start
     AND a.logged_datetime <= v_end
     --AND a.status_id != v_closedStatusId
     ORDER BY request_id,
   action_id;
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
   v_requestId NUMBER(10,0);
   v_actionId NUMBER(10,0);
   v_reqId NUMBER(10,0);
   v_prevActionId NUMBER(10,0);
BEGIN
   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   OPEN myCursor;

   v_reqId := 0;

   v_prevActionId := 0;

   FETCH myCursor INTO v_requestId,v_actionId;

   v_reqId := 0;

   v_prevActionId := 0;

   WHILE ( sqlserver_utilities.fetch_status(myCursor%FOUND) = 0 )
   LOOP
      BEGIN
         IF ( v_reqId = 0 ) THEN
         BEGIN
            v_reqId := v_requestId;

            v_prevActionId := v_actionId;

            INSERT INTO tt_tmpm
              VALUES ( v_systemId, v_requestId, v_actionId );

         END;
         ELSE
         BEGIN
            -- Check if we are dealing with the same request.
            IF ( v_reqId = v_requestId ) THEN
            BEGIN
               IF ( v_actionId != (v_prevActionId + 1) ) THEN
               BEGIN
                  DBMS_OUTPUT.PUT_LINE(v_requestId);

                  DBMS_OUTPUT.PUT_LINE(v_actionId);

                  DBMS_OUTPUT.PUT_LINE(v_prevActionId);

                  DBMS_OUTPUT.PUT_LINE('

                  ');

                  INSERT INTO tt_tmpm
                    VALUES ( v_systemId, v_requestId, v_actionId );

               END;
               END IF;

               v_prevActionId := v_actionId;

            END;
            ELSE
            BEGIN
               v_reqId := v_requestId;

               v_prevActionId := v_actionId;

               INSERT INTO tt_tmpm
                 VALUES ( v_systemId, v_requestId, v_actionId );

            END;
            END IF;

         END;
         END IF;

         FETCH myCursor INTO v_requestId,v_actionId;

      END;
   END LOOP;

   CLOSE myCursor;

   OPEN cv_1 FOR
      SELECT COUNT(*)
        FROM tt_tmpm t;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpm ';

END
;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_GETUREQUESTS" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
   --IF(
   --     @systemId != 106 AND      --GBO
   --     @systemId != 109 AND      --FinopHyd
   --     @systemId != 115 AND     --CashTrack
   --     @systemId != 201 AND     --tBits
   --     @systemId != 228 AND     --GBOTact
   --     @systemId != 105          --GenDev
   --  )
   --BEGIN
   --     SELECT @systemId = -1
   --END
   v_closedStatusId NUMBER(10,0);
BEGIN
   SELECT TYPE_ID
     INTO v_closedStatusId
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = 4
              AND NAME LIKE 'close%';

   DELETE FROM tt_tmpx1;

   INSERT INTO tt_tmpx1 (
      SELECT DISTINCT request_id
        FROM request_users ru
         WHERE ru.sys_id = v_systemId
                 AND ru.user_type_id = 3 );

   DELETE FROM tt_tmpx;

   INSERT INTO tt_tmpx (
      SELECT DISTINCT r.request_id
        FROM requests r
         WHERE r.sys_id = v_systemId
                 AND r.request_id NOT IN ( (
                                           SELECT *
                                           FROM tt_tmpx1))
                 AND r.status_id != v_closedStatusId );

   OPEN cv_1 FOR
      SELECT req.sys_id,
             req.request_id,
             req.category_id,
             req.status_id,
             req.severity_id,
             req.request_type_id,
             req.SUBJECT,
             req.DESCRIPTION "description",
             req.is_private,
             req.parent_request_id,
             req.USER_ID,
             req.max_action_id,
             req.due_datetime,
             req.logged_datetime,
             req.lastupdated_datetime,
             NVL(req.header_description, '''') "header_description",
             NVL(req.attachments, '''') "attachments",
             NVL(req.summary, '''') "summary",
             NVL(req.memo, '''') "memo",
             req.append_interface,
             req.notify,
             req.notify_loggers,
             req.replied_to_action,
             req.office_id
        FROM requests req
         WHERE req.sys_id = v_systemId
                 AND req.request_id IN ( SELECT *
                                         FROM tt_tmpx  )
        ORDER BY req.request_id DESC;

   OPEN cv_2 FOR
      -- Get the Corresponding request-user objects
      SELECT *
        FROM request_users ru
         WHERE ru.sys_id = v_systemId
                 AND ru.request_id IN ( SELECT *
                                        FROM tt_tmpx  )
        ORDER BY ru.request_id DESC;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REPORT_REQUESTSWORKEDON" 
/****** Object:  StoredProcedure [dbo].[stp_report_requestsWorkedOn]    Script Date: 04/17/2008 02:24:58 ******/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_startTime IN VARCHAR2 DEFAULT NULL ,
  v_endTime IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   DELETE FROM tt_tmp;

   INSERT INTO tt_tmp (
      --IF(
      --     @systemId != 106 AND      --GBO
      --     @systemId != 109 AND      --FinopHyd
      --     @systemId != 115 AND     --CashTrack
      --     @systemId != 201 AND     --tBits
      --     @systemId != 228 AND     --GBOTact
      --     @systemId != 105          --GenDev
      --  )
      --BEGIN
      --     SELECT @systemId = -1
      --END
      SELECT DISTINCT r.request_id
        FROM requests r
               JOIN actions a
                ON r.sys_id = a.sys_id
               AND r.request_id = a.request_id
         WHERE r.sys_id = v_systemId
                 AND r.request_id = a.request_id
                 AND a.lastupdated_datetime >= v_startTime
                 AND a.lastupdated_datetime <= v_endTime );

   OPEN cv_1 FOR
      SELECT req.sys_id,
             req.request_id,
             req.category_id,
             req.status_id,
             req.severity_id,
             req.request_type_id,
             req.SUBJECT,
             req.DESCRIPTION "description",
             req.is_private,
             req.parent_request_id,
             req.USER_ID,
             req.max_action_id,
             req.due_datetime,
             req.logged_datetime,
             req.lastupdated_datetime,
             NVL(req.header_description, '''') "header_description",
             NVL(req.attachments, '''') "attachments",
             NVL(req.summary, '''') "summary",
             NVL(req.memo, '''') "memo",
             req.append_interface,
             req.notify,
             req.notify_loggers,
             req.replied_to_action,
             req.office_id
        FROM requests req
         WHERE req.sys_id = v_systemId
                 AND req.request_id IN ( SELECT *
                                         FROM tt_tmp  )
        ORDER BY req.request_id DESC;

   OPEN cv_2 FOR
      -- Get the Corresponding request-user objects
      SELECT *
        FROM request_users ru
         WHERE ru.sys_id = v_systemId
                 AND ru.request_id IN ( SELECT *
                                        FROM tt_tmp  )
        ORDER BY ru.request_id DESC;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_AC_LUPBYSYSACTIONID" /*stp_req_action_lookupBySystemIdAndRequestIdAndActionId*/
/*CREATE OR REPLACE PROCEDURE stp_req_action_lookupBySystemIdAndRequestIdAndActionId*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR,
  cv_5 IN OUT SYS_REFCURSOR,
  cv_6 IN OUT SYS_REFCURSOR
)
AS
   v_parentRequestId NUMBER(10,0);
   --get all related requests
   v_sysPrefix VARCHAR2(50);
   v_temp NUMBER(1, 0) := 0;
BEGIN
   SELECT parent_request_id
     INTO v_parentRequestId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             category_id,
             status_id,
             severity_id,
             request_type_id,
             SUBJECT,
             DESCRIPTION,
             is_private,
             v_parentRequestId "parent_request_id",
             USER_ID,
             action_id "max_action_id",
             due_datetime,
             logged_datetime,
             lastupdated_datetime,
             header_description,
             attachments,
             summary,
             '' "memo",
             append_interface,
             notify,
             notify_loggers,
             replied_to_action,
             office_id
        FROM actions
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND action_id = v_actionId;

   OPEN cv_2 FOR
      SELECT *
        FROM action_users
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND action_id = v_actionId
        ORDER BY user_type_id,
                 ordering;

   OPEN cv_3 FOR
      --get sub_requests
      SELECT request_id,
                  SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = v_requestId
                 AND v_requestId <> 0;

   OPEN cv_4 FOR
      --get siblings
      SELECT request_id,
                  SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = ( SELECT parent_request_id
                                           FROM requests
                                              WHERE sys_id = v_systemId
                                                      AND request_id = v_requestId )
                 AND request_id <> v_requestId
                 AND parent_request_id <> 0;

   SELECT sys_prefix
     INTO v_sysPrefix
     FROM business_areas
      WHERE sys_id = v_systemId;

   DELETE FROM tt_tmpb1;

   INSERT INTO tt_tmpb1 (
      SELECT DISTINCT related_sys_prefix "sys_prefix",
                      related_request_id "request_id",
                      related_action_id "action_id",
                      to_char('primary') "subject"
        FROM related_requests
         WHERE primary_sys_prefix = v_sysPrefix
                 AND primary_request_id = v_requestId );

   INSERT INTO tt_tmpb1
     ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                       primary_request_id "request_id",
                       primary_action_id "action_id",
                       'secondary' "subject"
       FROM related_requests
          WHERE related_sys_prefix = v_sysPrefix
                  AND related_request_id = v_requestId );

   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE ( ( SELECT COUNT(*)
                 FROM tt_tmpb1  ) > 0 );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   -- #tmp1 contains all directly related request as of now
   -- get transitive related requests into #tmp1 if any
   IF v_temp = 1 THEN
   DECLARE
      v_temp NUMBER(1, 0) := 0;
   BEGIN
      DELETE FROM tt_tmpb2;

      INSERT INTO tt_tmpb2 (
         SELECT *
           FROM tt_tmpb1  );

      LOOP
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE ( ( SELECT COUNT(*)
                       FROM tt_tmpb2  ) > 0 );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp != 1 THEN
            EXIT;
         END IF;

         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            DELETE FROM tt_tmpb3;

            INSERT INTO tt_tmpb3 (
               SELECT DISTINCT related_sys_prefix "sys_prefix",
                               related_request_id "request_id",
                               related_action_id "action_id",
                               'transitive' "subject"
                 FROM related_requests ,
                      tt_tmpb2
                  WHERE primary_sys_prefix = tt_tmpb2.sys_prefix
                          AND primary_request_id = tt_tmpb2.request_id
                          AND ( NOT ( related_sys_prefix = v_sysPrefix
                          AND related_request_id = v_requestId ) )
                          AND ( NOT ( related_sys_prefix = tt_tmpb2.sys_prefix
                          AND related_request_id = tt_tmpb2.request_id ) ) );

            INSERT INTO tt_tmpb3
              ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                                primary_request_id "request_id",
                                primary_action_id "action_id",
                                'transitive' "subject"
                FROM related_requests ,
                     tt_tmpb2
                   WHERE related_sys_prefix = tt_tmpb2.sys_prefix
                           AND related_request_id = tt_tmpb2.request_id
                           AND ( NOT ( primary_sys_prefix = v_sysPrefix
                           AND primary_request_id = v_requestId ) )
                           AND ( NOT ( primary_sys_prefix = tt_tmpb2.sys_prefix
                           AND primary_request_id = tt_tmpb2.request_id ) ) );

            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT COUNT(*)
                          FROM tt_tmpb3  ) = 0 );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               EXIT;

            END;
            END IF;

            DELETE tt_tmpb2
            ;

            INSERT INTO tt_tmpb2
              ( SELECT *
                FROM tt_tmpb3
                   WHERE (sys_prefix || '#' || to_char(request_id)) NOT IN ( SELECT sys_prefix || '#' || to_char(request_id)
                                                                                                                 FROM tt_tmpb1  ) );

            INSERT INTO tt_tmpb1
              ( SELECT *
                FROM tt_tmpb3  );

            EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpb3 ';

         END;
      END LOOP;

      EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpb2 ';

   END;
   END IF;

   OPEN cv_5 FOR
      -- return all direct and transitive related requests
      SELECT DISTINCT CASE
                                WHEN tt_tmpb1.action_id > 0 THEN tt_tmpb1.sys_prefix || '#' || to_char(tt_tmpb1.request_id) || '#' || to_char(tt_tmpb1.action_id)
                           ELSE tt_tmpb1.sys_prefix || '#' || to_char(tt_tmpb1.request_id)
                              END "request_id",
                           SUBJECT
        FROM tt_tmpb1 ;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpb1 ';

   OPEN cv_6 FOR
      SELECT v_requestId "request_id",
             SUBJECT
        FROM requests
         WHERE request_id = v_parentRequestId
                 AND sys_id = v_systemId;

END

;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_DEL_ALL_RES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   DELETE action_users
   ;

   DELETE request_users
   ;

   OPEN cv_1 FOR
      SELECT *
        FROM user_read_actions ;

   DELETE user_read_actions
   ;

   DELETE transferred_requests
   ;

   DELETE user_drafts
   ;

   DELETE related_requests
   ;

   DELETE actions_ex
   ;

   DELETE actions
   ;

   DELETE requests_ex
   ;

   DELETE requests
   ;

   UPDATE business_areas
      SET max_request_id = 0;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_GETCHBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT request_id "childId",
             parent_request_id "parentId"
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id <> 0;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_LBYSREQIDFORVIEWREQ" 
/*stp_request_lookupBySystemIdAndRequestIdForViewRequest*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR,
  cv_5 IN OUT SYS_REFCURSOR,
  cv_6 IN OUT SYS_REFCURSOR,
  cv_7 IN OUT SYS_REFCURSOR
)
AS
   v_sysPrefix VARCHAR2(50);
   v_temp NUMBER(1, 0) := 0;
   v_parentRequestId NUMBER(10,0);
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             category_id,
             status_id,
             severity_id,
             request_type_id,
             SUBJECT,
             DESCRIPTION,
             is_private,
             parent_request_id,
             USER_ID,
             max_action_id,
             due_datetime,
             logged_datetime,
             lastupdated_datetime,
             header_description,
             attachments,
             summary,
             '' "memo",
             append_interface,
             notify,
             notify_loggers,
             replied_to_action,
             office_id
        FROM requests
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId;

   OPEN cv_2 FOR
      SELECT *
        FROM request_users
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
        ORDER BY user_type_id,
                 ordering;

   OPEN cv_3 FOR
      SELECT sys_id,
             request_id,
             field_id,
             bit_value,
             datetime_value,
             int_value,
             to_char(real_value) "real_value",
             varchar_value,
             text_value,
             type_value
        FROM requests_ex
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId;

   OPEN cv_4 FOR
      --get sub_requests
      SELECT request_id,
                   SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = v_requestId
                 AND v_requestId <> 0;

   OPEN cv_5 FOR
      --get siblings
      SELECT request_id,
                   SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = ( SELECT parent_request_id
                                           FROM requests
                                              WHERE sys_id = v_systemId
                                                      AND request_id = v_requestId )
                 AND request_id <> v_requestId
                 AND parent_request_id <> 0;

   --get related requests
   SELECT sys_prefix
     INTO v_sysPrefix
     FROM business_areas
      WHERE sys_id = v_systemId;

   DELETE FROM tt_tmpz1;

   INSERT INTO tt_tmpz1 (
      SELECT DISTINCT related_sys_prefix "sys_prefix",
                      related_request_id "request_id",
                      related_action_id "action_id"
        FROM related_requests
         WHERE primary_sys_prefix = v_sysPrefix
                 AND primary_request_id = v_requestId );

   INSERT INTO tt_tmp1
     ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                       primary_request_id "request_id",
                       primary_action_id "action_id"
       FROM related_requests
          WHERE related_sys_prefix = v_sysPrefix
                  AND related_request_id = v_requestId );

   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE ( ( SELECT COUNT(*)
                 FROM tt_tmpz1  ) > 0 );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   -- #tmp1 contains all directly related request as of now
   -- get transitive related requests into #tmp1 if any
   IF v_temp = 1 THEN
   DECLARE
      v_temp NUMBER(1, 0) := 0;
   BEGIN
      DELETE FROM tt_tmpz2;

      INSERT INTO tt_tmpz2 (
         SELECT *
           FROM tt_tmpz1  );

      LOOP
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE ( ( SELECT COUNT(*)
                       FROM tt_tmpz2  ) > 0 );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp != 1 THEN
            EXIT;
         END IF;

         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            DELETE FROM tt_tmpz3;

            INSERT INTO tt_tmpz3 (
               SELECT DISTINCT related_sys_prefix "sys_prefix",
                               related_request_id "request_id",
                               related_action_id "action_id"
                 FROM related_requests ,
                      tt_tmpz2
                  WHERE primary_sys_prefix = tt_tmpz2.sys_prefix
                          AND primary_request_id = tt_tmpz2.request_id
                          AND ( NOT ( related_sys_prefix = v_sysPrefix
                          AND related_request_id = v_requestId ) )
                          AND ( NOT ( related_sys_prefix = tt_tmp2.sys_prefix
                          AND related_request_id = tt_tmpz2.request_id ) ) );

            INSERT INTO tt_tmpz3
              ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                                primary_request_id "request_id",
                                primary_action_id "action_id"
                FROM related_requests ,
                     tt_tmpz2
                   WHERE related_sys_prefix = tt_tmpz2.sys_prefix
                           AND related_request_id = tt_tmpz2.request_id
                           AND ( NOT ( primary_sys_prefix = v_sysPrefix
                           AND primary_request_id = v_requestId ) )
                           AND ( NOT ( primary_sys_prefix = tt_tmp2.sys_prefix
                           AND primary_request_id = tt_tmpz2.request_id ) ) );

            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT COUNT(*)
                          FROM tt_tmpz3  ) = 0 );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               EXIT;

            END;
            END IF;

            DELETE tt_tmpz2
            ;

            INSERT INTO tt_tmpz2
              ( SELECT *
                FROM tt_tmpz3
                   WHERE (sys_prefix || '#' || to_char(request_id)) NOT IN ( SELECT sys_prefix || '#' || to_char(request_id)
                                                                                                                 FROM tt_tmpz1  ) );
                      
            INSERT INTO tt_tmpz1
              ( SELECT *
                FROM tt_tmpz3  );

            EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpz3 ';

         END;
      END LOOP;

      EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpz2 ';

   END;
   END IF;

   OPEN cv_6 FOR
      -- return all direct and transitive related requests
      SELECT DISTINCT CASE
                                 WHEN tt_tmp1.action_id > 0 THEN tt_tmp1.sys_prefix || '#' || to_char(tt_tmp1.request_id) || '#' || to_char(tt_tmp1.action_id)
                            ELSE tt_tmp1.sys_prefix || '#' || to_char(tt_tmp1.request_id)
                               END "request_id",
                            NVL(requests.SUBJECT, ' ') "subject"
        FROM tt_tmp1
               LEFT JOIN business_areas
                ON tt_tmp1.sys_prefix = business_areas.sys_prefix
               LEFT JOIN requests
                ON tt_tmp1.request_id = requests.request_id
               AND business_areas.sys_id = requests.sys_id;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp1 ';

   SELECT parent_request_id
     INTO v_parentRequestId
     FROM requests
      WHERE request_id = v_requestId
              AND sys_id = v_systemId;

   OPEN cv_7 FOR
      SELECT v_requestId "request_id",
             SUBJECT
        FROM requests
         WHERE request_id = v_parentRequestId
                 AND sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_LPBYSREQUESTID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR,
  cv_5 IN OUT SYS_REFCURSOR,
  cv_6 IN OUT SYS_REFCURSOR,
  cv_7 IN OUT SYS_REFCURSOR
)
AS
   --get all related requests
   v_sysPrefix VARCHAR2(50);
   v_temp NUMBER(1, 0) := 0;
   v_parentRequestId NUMBER(10,0);
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             request_id,
             category_id,
             status_id,
             severity_id,
             request_type_id,
             SUBJECT,
             DESCRIPTION,
             is_private,
             parent_request_id,
             USER_ID,
             max_action_id,
             due_datetime,
             logged_datetime,
             lastupdated_datetime,
             header_description,
             attachments,
             summary,
             '' "memo",
             append_interface,
             notify,
             notify_loggers,
             replied_to_action,
             office_id
        FROM requests
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId;

   OPEN cv_2 FOR
      SELECT *
        FROM request_users
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
        ORDER BY user_type_id,
                 ordering;

   OPEN cv_3 FOR
      SELECT sys_id,
             request_id,
             field_id,
             bit_value,
             datetime_value,
             int_value,
             sqlserver_utilities.convert('VARCHAR2(1024)', real_value) "real_value",
             varchar_value,
             text_value,
             type_value
        FROM requests_ex
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId;

   OPEN cv_4 FOR
      --get sub_requests
      SELECT request_id,
                   SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = v_requestId
                 AND v_requestId <> 0;

   OPEN cv_5 FOR
      --get siblings
      SELECT request_id,
                   SUBJECT
        FROM requests
         WHERE sys_id = v_systemId
                 AND parent_request_id = ( SELECT parent_request_id
                                           FROM requests
                                              WHERE sys_id = v_systemId
                                                      AND request_id = v_requestId )
                 AND request_id <> v_requestId
                 AND parent_request_id <> 0;

   SELECT sys_prefix
     INTO v_sysPrefix
     FROM business_areas
      WHERE sys_id = v_systemId;

   DELETE FROM tt_tmp1;

   INSERT INTO tt_tmp1 (
      SELECT DISTINCT related_sys_prefix "sys_prefix",
                      related_request_id "request_id",
                      related_action_id "action_id",
                      sqlserver_utilities.convert('VARCHAR2(50)', 'primary') "subject"
        FROM related_requests
         WHERE primary_sys_prefix = v_sysPrefix
                 AND primary_request_id = v_requestId );

   INSERT INTO tt_tmp1
     ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                       primary_request_id "request_id",
                       primary_action_id "action_id",
                       'secondary' "subject"
       FROM related_requests
          WHERE related_sys_prefix = v_sysPrefix
                  AND related_request_id = v_requestId );

   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE ( ( SELECT COUNT(*)
                 FROM tt_tmp1  ) > 0 );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   -- #tmp1 contains all directly related request as of now
   -- get transitive related requests into #tmp1 if any
   IF v_temp = 1 THEN
   DECLARE
      v_temp NUMBER(1, 0) := 0;
   BEGIN
      DELETE FROM tt_tmp2;

      INSERT INTO tt_tmp2 (
         SELECT *
           FROM tt_tmp1  );

      LOOP
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE ( ( SELECT COUNT(*)
                       FROM tt_tmp2  ) > 0 );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp != 1 THEN
            EXIT;
         END IF;

         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            DELETE FROM tt_tmp3;

            INSERT INTO tt_tmp3 (
               SELECT DISTINCT related_sys_prefix "sys_prefix",
                               related_request_id "request_id",
                               related_action_id "action_id",
                               'transitive' "subject"
                 FROM related_requests ,
                      tt_tmp2
                  WHERE primary_sys_prefix = tt_tmp2.sys_prefix
                          AND primary_request_id = tt_tmp2.request_id
                          AND ( NOT ( related_sys_prefix = v_sysPrefix
                          AND related_request_id = v_requestId ) )
                          AND ( NOT ( related_sys_prefix = tt_tmp2.sys_prefix
                          AND related_request_id = tt_tmp2.request_id ) ) );

            INSERT INTO tt_tmp3
              ( SELECT DISTINCT primary_sys_prefix "sys_prefix",
                                primary_request_id "request_id",
                                primary_action_id "action_id",
                                'transitive' "subject"
                FROM related_requests ,
                     tt_tmp2
                   WHERE related_sys_prefix = tt_tmp2.sys_prefix
                           AND related_request_id = tt_tmp2.request_id
                           AND ( NOT ( primary_sys_prefix = v_sysPrefix
                           AND primary_request_id = v_requestId ) )
                           AND ( NOT ( primary_sys_prefix = tt_tmp2.sys_prefix
                           AND primary_request_id = tt_tmp2.request_id ) ) );

            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT COUNT(*)
                          FROM tt_tmp3  ) = 0 );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               EXIT;

            END;
            END IF;

            DELETE tt_tmp2
            ;

            INSERT INTO tt_tmp2
              ( SELECT *
                FROM tt_tmp3
                  WHERE (sys_prefix || '#' || sqlserver_utilities.convert('VARCHAR2(50)', request_id)) NOT IN ( SELECT sys_prefix || '#' || sqlserver_utilities.convert('VARCHAR2(50)', request_id)
                                                                                                                 FROM tt_tmp1  ) );

            INSERT INTO tt_tmp1
              ( SELECT *
                FROM tt_tmp3  );

            EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp3 ';

         END;
      END LOOP;

      EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp2 ';

   END;
   END IF;

   OPEN cv_6 FOR
      -- return all direct and transitive related requests
      SELECT DISTINCT CASE
                                 WHEN tt_tmp1.action_id > 0 THEN tt_tmp1.sys_prefix || '#' || sqlserver_utilities.convert('VARCHAR2(50)', tt_tmp1.request_id) || '#' || sqlserver_utilities.convert('VARCHAR2(50)', tt_tmp1.action_id)
                            ELSE tt_tmp1.sys_prefix || '#' || sqlserver_utilities.convert('VARCHAR2(50)', tt_tmp1.request_id)
                               END "request_id",
                            SUBJECT
        FROM tt_tmp1 ;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp1 ';

   SELECT parent_request_id
     INTO v_parentRequestId
     FROM requests
      WHERE request_id = v_requestId
              AND sys_id = v_systemId;

   OPEN cv_7 FOR
      SELECT v_requestId "request_id",
             SUBJECT
        FROM requests
         WHERE request_id = v_parentRequestId
                 AND sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQ_LUPBYSREQUESTDATA" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_subject IN NVARCHAR2 DEFAULT NULL ,
  v_updatedtime IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT request_id
        FROM requests
         WHERE sys_id = v_systemId
                 AND USER_ID = v_userId
                 AND SUBJECT = v_subject
               AND ABS( lastupdated_datetime- v_updatedtime) < 5;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_GETPRIVACY" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_private OUT NUMBER
)
AS
   v_isPrivate NUMBER(1,0);
   v_parentId NUMBER(10,0);
   v_fieldId NUMBER(10,0);
   v_typeId NUMBER(10,0);
BEGIN
   SELECT is_private
     INTO v_isPrivate
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   IF ( v_isPrivate = 1 ) THEN
   BEGIN
      v_private := 1;

      RETURN;

   END;
   END IF;

   SELECT category_id
     INTO v_typeId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   SELECT field_id
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'category_id';

   SELECT is_private
     INTO v_isPrivate
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = v_fieldId
              AND TYPE_ID = v_typeId;

   IF ( v_isPrivate = 1 ) THEN
   BEGIN
      v_private := 1;

      RETURN;

   END;
   END IF;

   SELECT status_id
     INTO v_typeId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   SELECT field_id
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'status_id';

   SELECT is_private
     INTO v_isPrivate
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = v_fieldId
              AND TYPE_ID = v_typeId;

   IF ( v_isPrivate = 1 ) THEN
   BEGIN
      v_private := 1;

      RETURN;

   END;
   END IF;

   SELECT severity_id
     INTO v_typeId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   SELECT field_id
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'severity_id';

   SELECT is_private
     INTO v_isPrivate
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = v_fieldId
              AND TYPE_ID = v_typeId;

   IF ( v_isPrivate = 1 ) THEN
   BEGIN
      v_private := 1;

      RETURN;

   END;
   END IF;

   SELECT request_type_id
     INTO v_typeId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   SELECT field_id
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'request_type_id';

   SELECT is_private
     INTO v_isPrivate
     FROM types
      WHERE sys_id = v_systemId
              AND field_id = v_fieldId
              AND TYPE_ID = v_typeId;

   IF ( v_isPrivate = 1 ) THEN
   BEGIN
      v_private := 1;

      RETURN;

   END;
   END IF;

   SELECT parent_request_id
     INTO v_parentId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   WHILE ( v_parentId != 0 )
   LOOP
      BEGIN
         SELECT is_private
           INTO v_isPrivate
           FROM requests
            WHERE sys_id = v_systemId
                    AND request_id = v_parentId;

         IF ( v_isPrivate = 1 ) THEN
         BEGIN
            v_private := 1;

            RETURN;

         END;
         END IF;

         SELECT parent_request_id
           INTO v_parentId
           FROM requests
            WHERE sys_id = v_systemId
                    AND request_id = v_parentId;

      END;
   END LOOP;

   v_isPrivate := 0;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_INSERT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId OUT NUMBER,
  v_categoryId IN NUMBER DEFAULT NULL ,
  v_statusId IN NUMBER DEFAULT NULL ,
  v_severityId IN NUMBER DEFAULT NULL ,
  v_requestTypeId IN NUMBER DEFAULT NULL ,
  v_subject IN NVARCHAR2 DEFAULT NULL ,
  v_description IN CLOB DEFAULT NULL ,
  v_IsPrivate IN NUMBER DEFAULT NULL ,
  v_parentId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  iv_maxActionId IN NUMBER DEFAULT NULL ,
  v_dueDate IN DATE DEFAULT NULL ,
  v_loggedDate IN DATE DEFAULT NULL ,
  v_updatedDate IN DATE DEFAULT NULL ,
  v_headerDesc IN CLOB DEFAULT NULL ,
  v_attachments IN CLOB DEFAULT NULL ,
  v_summary IN CLOB DEFAULT NULL ,
  v_memo IN CLOB DEFAULT NULL ,
  v_append IN NUMBER DEFAULT NULL ,
  v_notify IN NUMBER DEFAULT NULL ,
  v_notifyLoggers IN NUMBER DEFAULT NULL ,
  v_repliedToAction IN NUMBER DEFAULT NULL ,
  v_officeId IN NUMBER DEFAULT NULL
)
AS
   v_maxActionId NUMBER(10,0) := iv_maxActionId;
BEGIN
   --- Read the max request id from business areas and add one to it.
   SELECT NVL(max_request_id, 0) + 1
     INTO v_requestId
     FROM business_areas
      WHERE sys_id = v_systemId;

   --- We cannot hold this value of max_request_id with us till the end of this transaction
   --- as other processes might be interested in inserting requests.
   --- So, update the business_areas table with this new max_request_id value.
   UPDATE business_areas
      SET max_request_id = v_requestId
      WHERE sys_id = v_systemId;

   --- Max Action Id is always 1 when inserting the request.
   v_maxActionId := 1;

   --- Now insert the request.
   INSERT INTO requests
     ( sys_id, request_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, max_action_id, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action, office_id )
     VALUES ( v_systemId, v_requestId, v_categoryId, v_statusId, v_severityId, v_requestTypeId, v_subject, v_description, v_IsPrivate, v_parentId, v_userId, v_maxActionId, v_dueDate, v_loggedDate, v_updatedDate, v_headerDesc, v_attachments, v_summary, v_memo, v_append, v_notify, v_notifyLoggers, v_repliedToAction, v_officeId );

   ---- Insert the corresponding record into actions table.
   INSERT INTO actions
     ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action, office_id )
     VALUES ( v_systemId, v_requestId, v_maxActionId, v_categoryId, v_statusId, v_severityId, v_requestTypeId, v_subject, v_description, v_IsPrivate, v_parentId, v_userId, v_dueDate, v_loggedDate, v_updatedDate, v_headerDesc, v_attachments, v_summary, v_memo, v_append, v_notify, v_notifyLoggers, v_repliedToAction, v_officeId );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_LOOKUPSUBJECT" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_email IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR
)
AS
   v_private NUMBER(1,0);
   v_privatePermission NUMBER(10,0);
BEGIN
   v_private := stp_request_getRequestPrivacy(v_systemId, v_requestId);

   IF ( v_private = 1 ) THEN
   BEGIN
      IF ( v_email = 1 ) THEN
      BEGIN
         RETURN;

      END;
      ELSE
      BEGIN
         v_privatePermission := stp_request_getUserPrivatePerm(v_systemId, v_requestId, v_userId);

         IF ( v_privatePermission > 3 ) THEN
         BEGIN
            OPEN cv_1 FOR
               SELECT SUBJECT
                 FROM requests
                  WHERE sys_id = v_systemId
                          AND request_id = v_requestId;

         END;
         END IF;

      END;
      END IF;

   END;
   ELSE
   BEGIN
      OPEN cv_2 FOR
         SELECT SUBJECT
           FROM requests
            WHERE sys_id = v_systemId
                    AND request_id = v_requestId;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_UPDATE" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_categoryId IN NUMBER DEFAULT NULL ,
  v_statusId IN NUMBER DEFAULT NULL ,
  v_severityId IN NUMBER DEFAULT NULL ,
  v_requestTypeId IN NUMBER DEFAULT NULL ,
  v_subject IN NVARCHAR2 DEFAULT NULL ,
  v_description IN CLOB DEFAULT NULL ,
  v_IsPrivate IN NUMBER DEFAULT NULL ,
  v_parentId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_maxActionId OUT NUMBER,
  v_dueDate IN DATE DEFAULT NULL ,
  v_loggedDate IN DATE DEFAULT NULL ,
  v_updatedDate IN DATE DEFAULT NULL ,
  v_headerDesc IN CLOB DEFAULT NULL ,
  v_attachments IN CLOB DEFAULT NULL ,
  v_summary IN CLOB DEFAULT NULL ,
  v_memo IN CLOB DEFAULT NULL ,
  v_append IN NUMBER DEFAULT NULL ,
  v_notify IN NUMBER DEFAULT NULL ,
  v_notifyLoggers IN NUMBER DEFAULT NULL ,
  v_repliedToAction IN NUMBER DEFAULT NULL ,
  v_officeId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   --- Read the max action id from requests and add one to it.
   SELECT NVL(max_action_id, 0) + 1
     INTO v_maxActionId
     FROM requests
      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

   --- We cannot hold this value of max_action_id with us till the end of this transaction
   --- as other processes might be interested in inserting actions in this request in the meantime.
   --- So, update the requests table with this new max_action_id value.
   UPDATE requests
      SET max_action_id = v_maxActionId
      WHERE sys_id = v_systemId
     AND request_id = v_requestId;

   --- Now update the request.
   UPDATE requests
      SET sys_id = v_systemId,
          request_id = v_requestId,
          category_id = v_categoryId,
          status_id = v_statusId,
          severity_id = v_severityId,
          request_type_id = v_requestTypeId,
          SUBJECT = v_subject,
          DESCRIPTION = v_description,
          is_private = v_isPrivate,
          parent_request_id = v_parentId,
          USER_ID = v_userId,
          max_action_id = v_maxActionId,
          due_datetime = v_dueDate,
          logged_datetime = v_loggedDate,
          lastupdated_datetime = v_updatedDate,
          header_description = v_headerDesc,
          attachments = v_attachments,
          memo = v_memo,
          append_interface = v_append,
          notify = v_notify,
          notify_loggers = v_notifyLoggers,
          replied_to_action = v_repliedToAction,
          office_id = v_officeId
      WHERE sys_id = v_systemId
     AND request_id = v_requestId;

   -- If summary is NOT NULL, then it should be updated in the request.
   -- Otherwise retain the old subject.
   IF ( v_summary IS NOT NULL ) THEN
   BEGIN
      UPDATE requests
         SET summary = v_summary
         WHERE sys_id = v_systemId
        AND request_id = v_requestId;

   END;
   END IF;

   ---- Insert the corresponding record into actions table.
   INSERT INTO actions
     ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action, office_id )
     VALUES ( v_systemId, v_requestId, v_maxActionId, v_categoryId, v_statusId, v_severityId, v_requestTypeId, v_subject, v_description, v_IsPrivate, v_parentId, v_userId, v_dueDate, v_loggedDate, v_updatedDate, v_headerDesc, v_attachments, v_summary, v_memo, v_append, v_notify, v_notifyLoggers, v_repliedToAction, v_officeId );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_UPDATEATTACHMENTS" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_attachments IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE requests
      SET attachments = v_attachments
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND max_action_id = v_actionId;

   UPDATE actions
      SET attachments = v_attachments
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_REQUEST_UPDATEHEADERDESC" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_headerDesc IN CLOB DEFAULT NULL
)
AS
BEGIN
   UPDATE requests
      SET header_description = v_headerDesc
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND max_action_id = v_actionId;

   UPDATE actions
      SET header_description = v_headerDesc
      WHERE sys_id = v_systemId
     AND request_id = v_requestId
     AND action_id = v_actionId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RO_GSBYSYSIDANDREQDACTID" /* stp_roleperm_getAuthUsersBySystemIdAndRequestIdAndActionId */
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
BEGIN
   /*
 * Get the users directly in any role or directly related to the request
 */
   INSERT INTO tt_tmpUsers
     ( userId )
     ( SELECT ru.USER_ID
       FROM roles r
              JOIN roles_users ru
               ON r.sys_id = ru.sys_id
              AND r.role_id = ru.role_id
          WHERE ru.sys_id = v_sysId
                  AND ru.is_active = 1 );

   INSERT INTO tt_tmpUsers
     ( userId )
     ( SELECT au.USER_ID
       FROM action_users au
          WHERE au.sys_id = v_sysId
                  AND au.request_id = v_requestId
                  AND au.action_id = v_actionId );

   /*
 * Get the users of all mailing lists (present in #tmpUser i.e associated directly or indirectly to the request) 
 * recursively
 */
   /*
 * Get the first level of mailing lists (from #tmpUser) 
 */
   INSERT INTO tt_tmpp
     ( mailListId )
     ( SELECT mail_list_id
       FROM mail_list_users
          WHERE mail_list_id IN ( SELECT *
                                  FROM tt_tmpUsers  ) );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          /*
 * if first level exists, recusively get users mailing lists 
 */
          WHERE ( EXISTS ( SELECT *
                                 FROM tt_tmpp  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         INSERT INTO tt_tmpUsers
           ( userId )
           ( SELECT USER_ID
             FROM mail_list_users
                WHERE mail_list_id IN ( SELECT *
                                        FROM tt_tmpp  ) );

         DELETE FROM tt_tmpp1;

         INSERT INTO tt_tmpp1 (
            SELECT USER_ID
              FROM mail_list_users
               WHERE mail_list_id IN (  
                                       SELECT *
                                       FROM tt_tmpp   ) );

         DELETE tt_tmpp
         ;

         INSERT INTO tt_tmpp
           ( SELECT *
             FROM tt_tmpp1  );

         DELETE tt_tmpp1
         ;

         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpp1 ';

      END;
   END LOOP;

   DELETE tt_tmpp
   ;

   INSERT INTO tt_tmpp
     ( SELECT DISTINCT *
       FROM tt_tmpUsers  );

   DELETE tt_tmpUsers
   ;

   INSERT INTO tt_tmpUsers
     ( userId )
     ( SELECT DISTINCT *
       FROM tt_tmpp  );

   DELETE tt_tmpp
   ;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpp ';

   OPEN cv_1 FOR
      SELECT u.user_login,
             MAX(rp.gpermissions)
        FROM fields f
               JOIN roles_permissions rp
                ON f.sys_id = rp.sys_id
               AND f.field_id = rp.field_id
               AND f.NAME = 'is_private'
               JOIN roles_users ru
                ON ru.sys_id = rp.sys_id
               AND ru.role_id = rp.role_id
               AND ru.is_active = 1
               AND ru.USER_ID IN ( SELECT userId
                                   FROM tt_tmpUsers  )
               LEFT JOIN roles_permissions rqp
                ON f.sys_id = rqp.sys_id
               AND f.field_id = rqp.field_id
               AND f.NAME = 'is_private'
               LEFT JOIN action_users au
                ON rqp.sys_id = au.sys_id
               AND rqp.role_id = au.user_type_id
               AND au.request_id = v_requestId
               AND au.action_id = v_actionId
               AND au.USER_ID IN ( SELECT userId
                                   FROM tt_tmpUsers  )
               LEFT JOIN users u
                ON u.USER_ID = ru.USER_ID
               OR u.USER_ID = au.USER_ID
               OR u.USER_ID IN ( SELECT userId
                                 FROM tt_tmpUsers  )
               JOIN PERMISSIONS p
                ON p.permission = rp.gpermissions
         WHERE f.sys_id = v_sysId
                 AND rp.gpermissions >= 4
        GROUP BY u.user_login;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpUsers ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLE_PER_DEFPERMISSIONS" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_role_name IN VARCHAR2 DEFAULT NULL
)
AS
   v_max_field_id NUMBER(10,0);
   v_role_id NUMBER(10,0);
BEGIN
   SELECT MAX(field_id)
     INTO v_max_field_id
     FROM fields
      WHERE sys_id = v_sys_id;

   SELECT role_id
     INTO v_role_id
     FROM roles
      WHERE rolename = v_role_name
              AND sys_id = v_sys_id;

   IF ( v_role_id = 1 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 1;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 1;

   END;
   END IF;

   IF ( v_role_id = 2 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 2;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 2;

   END;
   END IF;

   IF ( v_role_id = 3 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 3;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 3;

   END;
   END IF;

   IF ( v_role_id = 4 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 4;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 4;

   END;
   END IF;

   IF ( v_role_id = 5 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 5;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 5;

   END;
   END IF;

   IF ( v_role_id = 6 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 6;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 6;

   END;
   END IF;

   IF ( v_role_id = 7 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 7;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 7;

   END;
   END IF;

   IF ( v_role_id = 8 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 8;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 8;

   END;
   END IF;

   IF ( v_role_id = 9 ) THEN
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 1
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 2
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 3
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 4
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 5
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 6
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 7
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 8
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 9
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 10
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 11
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 12
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 13
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 14
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 15
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 16
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 17
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 18
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 19
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 4,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 20
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 21
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 5,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 22
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 23
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 24
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 0,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 25
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 26
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 27
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 6,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 28
        AND role_id = 9;

      UPDATE roles_permissions
         SET gpermissions = 7,
             dpermissions = 0
         WHERE sys_id = v_sys_id
        AND field_id = 29
        AND role_id = 9;

   END;
   END IF;

   IF ( v_max_field_id > 29 ) THEN
   DECLARE
      v_i NUMBER(10,0);
   BEGIN
      v_i := 30;

      WHILE ( v_i <= v_max_field_id )
      LOOP
         BEGIN
            IF ( v_role_id = 1 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 5,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 1;

            END;
            END IF;

            IF ( v_role_id = 2 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 5,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 2;

            END;
            END IF;

            IF ( v_role_id = 3 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 6,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 3;

            END;
            END IF;

            IF ( v_role_id = 4 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 4,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 4;

            END;
            END IF;

            IF ( v_role_id = 5 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 4,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 5;

            END;
            END IF;

            IF ( v_role_id = 6 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 4,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 6;

            END;
            END IF;

            IF ( v_role_id = 7 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 7,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 7;

            END;
            END IF;

            IF ( v_role_id = 8 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 7,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 8;

            END;
            END IF;

            IF ( v_role_id = 9 ) THEN
            BEGIN
               UPDATE roles_permissions
                  SET gpermissions = 7,
                      dpermissions = 0
                  WHERE sys_id = v_sys_id
                 AND field_id = v_i
                 AND role_id = 9;

            END;
            END IF;

            v_i := v_i + 1;

         END;
      END LOOP;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GETPERBYSYSAIDANDUID" /* 0 use in java code--stp_roleperm_getPermissionsBySystemIdAndRequestIdAndActionIdAndUserId*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
BEGIN
   /*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
   /*
 * Get the mailing lists where the user is a direct member.
 */
   INSERT INTO tt_tmp
     ( mailListId )
     ( SELECT mail_list_id
       FROM mail_list_users
          WHERE USER_ID = v_userId );

   INSERT INTO tt_tmp1
     ( SELECT mailListId
       FROM tt_tmp  );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE ( EXISTS ( SELECT *
                           FROM tt_tmp1  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         DELETE FROM tt_tmp2;

         INSERT INTO tt_tmp2 (
            /*
      * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
      */
            SELECT mlu.mail_list_id "mailListId"
              FROM mail_list_users mlu
                     JOIN tt_tmp1 t1
                      ON mlu.USER_ID = t1.mailListId
                     LEFT JOIN tt_tmp t
                      ON mlu.mail_list_id = t.mailListId
               WHERE t.mailListId IS NULL );

         INSERT INTO tt_tmp1
           ( SELECT mailListId
             FROM tt_tmp2  );

         INSERT INTO tt_tmp
           ( SELECT mailListId
             FROM tt_tmp2  );

         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp2 ';

         DELETE tt_tmp1
         ;

      END;
   END LOOP;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp1 ';

   OPEN cv_1 FOR
      SELECT t.NAME "name",
             t.field_id "field_id",
             CASE SUM(p.padd)
                             WHEN 0 THEN 0
             ELSE 1
                END + CASE SUM(p.pchange)
                                         WHEN 0 THEN 0
             ELSE 2
                END + CASE SUM(p.pview)
                                       WHEN 0 THEN 0
             ELSE 4
                END "permission"
        FROM PERMISSIONS p
               /*
      * Get the permissions the user gets by virtue of being a user of the system.
      */
               JOIN ( SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                         WHERE rp.sys_id = v_systemId
                                 AND rp.role_id = 1
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of the BA.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN roles_users ru
                              ON ru.sys_id = rp.sys_id
                             AND ru.role_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( ru.USER_ID = v_userId
                                 OR ru.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmp ) )
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of this action of the request.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN action_users au
                              ON au.sys_id = v_systemId
                             AND au.request_id = v_requestId
                             AND au.action_id = v_actionId
                             AND au.user_type_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( au.USER_ID = v_userId
                                 OR au.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmp  ) ) ) t
                ON p.permission = t.gpermissions
        GROUP BY t.NAME,t.field_id;

   /*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GPBYSIDRIDAIDUIDLIST" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_userId IN VARCHAR2 DEFAULT NULL
)
AS
   v_query VARCHAR2(7999);
BEGIN
   v_query := '
   CREATE TABLE #tmp
   (
        mailListId INT,
        userId         INT
   )

   /*
    * Get the mailing lists where the user is a direct member.
    */
   INSERT INTO #tmp(mailListId,userId)
   select
        mail_list_id,user_id
   from
        mail_list_users
   where
        user_id in (' || v_userId || ')

   SELECT * INTO #tmp1 FROM #tmp
   WHILE (EXISTS(SELECT * FROM #tmp1))
   BEGIN
        /*
         * Get the mailing lists where the id in #tmp1 is a member.
            * which is already not part of #tmp
         */
        SELECT
             mlu.mail_list_id,t1.userId
        INTO #tmp2
        FROM
             mail_list_users mlu
             JOIN #tmp1 t1
             ON mlu.user_id = t1.mailListId
             LEFT JOIN #tmp t
             ON mlu.mail_list_id = t.mailListId
        WHERE
             t.mailListId IS NULL or (mlu.user_id = t1.mailListId and mlu.mail_list_id = t.mailListId)

        INSERT INTO #tmp1 SELECT * FROM #tmp2
        INSERT INTO #tmp SELECT * FROM #tmp2
        DROP TABLE #tmp2
        DELETE #tmp1
   END
   DROP TABLE #tmp1

   SELECT
        f.name ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id "userId"
        into #tmp3
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id
        LEFT JOIN users u
        ON u.user_id in (' || v_userId || ')
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char(v_sysId) || ' AND
        (
             rp.role_id = 1
        )


   Insert into #tmp3
   SELECT
        f.name  ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id "userId"
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id
        LEFT JOIN roles_users ru
        ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
        LEFT JOIN #tmp mlu
        ON mlu.mailListId = ru.user_id
        LEFT JOIN users u
        ON u.user_id = ru.user_id or mlu.userId = u.user_id
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char(v_sysId) || ' AND
        (
             u.user_id in (' || v_userId || ')
        )

   Insert into #tmp3
   SELECT
        f.name  ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id "userId"
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id
        LEFT JOIN action_users aq
        ON aq.sys_id = rp.sys_id AND aq.request_id = ' || to_char(v_requestId) || ' AND aq.action_id = ' || to_char(v_actionId) || '     and aq.user_type_id = rp.role_id
        LEFT JOIN #tmp mlu
        ON mlu.mailListId =aq.user_id
        LEFT JOIN users u
        ON u.user_id = aq.user_id or mlu.userId = u.user_id
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char(v_sysId) || ' AND
        (
             u.user_id in (' || v_userId || ')
        )


   select userId,
        case sum(CONVERT(INT,padd))
             when 0 then 0
             else 1
        end
        +
        case sum(CONVERT(INT, pchange))
             when 0 then 0
             else 2
        end
        +
        case sum(CONVERT(INT,pview))
             when 0 then 0
             else 4
        end  ''permission'',
        name,
        field_id
   into #tmp4
   from #tmp3
   GROUP BY name, field_id,userId

   select field_id, name, min(permission) ''permission''
   from #tmp4
   group by field_id,name
   order by field_id

   drop table #tmp3
   drop table #tmp4
   drop table #tmp
   ';

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GPBYSIDRIDUSERID" /*stp_roleperm_getPermissionsBySystemIdAndRequestIdAndUserId*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
BEGIN
   /*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
   /*
 * Get the mailing lists where the user is a direct member.
 */
   INSERT INTO tt_tmpa
     ( mailListId )
     ( SELECT mail_list_id
       FROM mail_list_users
          WHERE USER_ID = v_userId );

   DELETE FROM tt_tmpa1;

   INSERT INTO tt_tmpa1 (
      SELECT *
        FROM tt_tmpa );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE ( EXISTS ( SELECT *
                           FROM tt_tmpa1  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         DELETE FROM tt_tmpa2;

         INSERT INTO tt_tmpa2 (
            /*
      * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
      */
            SELECT mlu.mail_list_id
              FROM mail_list_users mlu
                     JOIN tt_tmpa1 t1
                      ON mlu.USER_ID = t1.mailListId
                     LEFT JOIN tt_tmpa t
                      ON mlu.mail_list_id = t.mailListId
               WHERE t.mailListId IS NULL );

         INSERT INTO tt_tmpa1
           ( SELECT *
             FROM tt_tmpa2  );

         INSERT INTO tt_tmpa
           ( SELECT *
             FROM tt_tmpa2  );

         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpa2 ';

         DELETE tt_tmpa1
         ;

      END;
   END LOOP;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpa1 ';

   OPEN cv_1 FOR
      SELECT CASE SUM(p.padd)
                  WHEN 0 THEN 0
             ELSE 1
                END + CASE SUM(p.pchange)
                   WHEN 0 THEN 0
             ELSE 2
                END + CASE SUM(p.pview)
                    WHEN 0 THEN 0
             ELSE 4
                END "permission",
             t.name "name",
             t.field_id "field_id"
        FROM PERMISSIONS p
               /*
      * Get the permissions the user gets by virtue of being a user of the system.
      */
               JOIN ( SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                         WHERE rp.sys_id = v_systemId
                                 AND rp.role_id = 1
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of the BA.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN roles_users ru
                              ON ru.sys_id = rp.sys_id
                             AND ru.role_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( ru.USER_ID = v_userId
                                 OR ru.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmpa  ) )
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of this request.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN request_users ru
                              ON ru.sys_id = v_systemId
                             AND ru.request_id = v_requestId
                             AND ru.user_type_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( ru.USER_ID = v_userId
                                 OR ru.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmpa  ) ) ) t
                ON p.permission = t.gpermissions
        GROUP BY t.NAME,t.field_id
      UNION
      /*
 * Get the list of application specific roles the user is present in.
 */
      SELECT -1,
                   CASE rolename
                                WHEN 'Analyst' THEN '__ROLE_ANALYST__'
                                WHEN 'Admin' THEN '__ADMIN__'
                                WHEN 'PermissionAdmin' THEN '__PERMISSIONADMIN__'
                   ELSE rolename
                      END,
                   10
        FROM roles_users ru
               JOIN roles r
                ON r.sys_id = ru.sys_id
               AND r.role_id = ru.role_id
               AND ru.is_active = 1
         WHERE ru.sys_id = v_systemId
                 AND ( ru.USER_ID = v_userId
                 OR ru.USER_ID IN ( SELECT mailListId
                                    FROM tt_tmpa  ) )
                 AND r.rolename IN ( 'Analyst','Admin','PermissionAdmin' )
      UNION
      /*
 * Check if the user is a part of super user list.
 */
      SELECT -1,
                   '__SUPER_USER__',
                   -1
        FROM super_users
         WHERE USER_ID = v_userId
                 AND is_active = 1;

   /*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpa ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GPBYSIDUSERID" /* stp_roleperm_getPermissionsBySystemIdAndUserId*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
   v_privateFieldId NUMBER(10,0);
BEGIN
   /*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
   /*
 * Get the mailing lists where the user is a direct member.
 */
   INSERT INTO tt_tmpr
     ( mailListId )
     ( SELECT mail_list_id
       FROM mail_list_users
          WHERE USER_ID = v_userId );

   DELETE FROM tt_tmpr1;

   INSERT INTO tt_tmpr1 (
      SELECT *
        FROM tt_tmpr  );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE ( EXISTS ( SELECT *
                           FROM tt_tmpr1  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         DELETE FROM tt_tmpr2;

         INSERT INTO tt_tmpr2 (
            /*
      * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
      */
            SELECT mlu.mail_list_id
              FROM mail_list_users mlu
                     JOIN tt_tmpr1 t1
                      ON mlu.USER_ID = t1.mailListId
                     LEFT JOIN tt_tmpr t
                      ON mlu.mail_list_id = t.mailListId
               WHERE t.mailListId IS NULL );

         INSERT INTO tt_tmpr1
           ( SELECT *
             FROM tt_tmpr2  );

         INSERT INTO tt_tmpr
           ( SELECT *
             FROM tt_tmpr2  );

         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpr2 ';

         DELETE tt_tmpr1
         ;

      END;
   END LOOP;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpr1 ';

   SELECT NVL(field_id, 0)
     INTO v_privateFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'is_private';

   OPEN cv_1 FOR
      SELECT t.NAME "name",
             t.field_id "field_id",
             CASE SUM(p.padd)
                      WHEN 0 THEN 0
             ELSE 1
                END + CASE SUM(p.pchange)
                                         WHEN 0 THEN 0
             ELSE 2
                END + CASE SUM(p.pview)
                                       WHEN 0 THEN 0
             ELSE 4
                END "permission"
        FROM PERMISSIONS p
               /*
      * Get the permissions the user gets by virtue of being a user of the system.
      */
               JOIN ( SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                         WHERE rp.sys_id = v_systemId
                                 AND rp.role_id = 1
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of the BA.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN roles_users ru
                              ON ru.sys_id = rp.sys_id
                             AND ru.role_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( ru.USER_ID = v_userId
                                 OR ru.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmpr  ) ) ) t
                ON p.permission = t.gpermissions
        GROUP BY t.NAME,t.field_id
      UNION
      /*
 * Get the list of application specific roles the user is present in.
 */
      SELECT CASE rolename
                                WHEN 'Analyst' THEN '__ROLE_ANALYST__'
                                WHEN 'Admin' THEN '__ADMIN__'
                                WHEN 'PermissionAdmin' THEN '__PERMISSIONADMIN__'
                   ELSE rolename
                      END,
                   -1,
                   10
        FROM roles_users ru
               JOIN roles r
                ON r.sys_id = ru.sys_id
               AND r.role_id = ru.role_id
               AND ru.is_active = 1
         WHERE ru.sys_id = v_systemId
                 AND ( ru.USER_ID = v_userId
                 OR ru.USER_ID IN ( SELECT mailListId
                                    FROM tt_tmpr  ) )
                 AND r.rolename IN ( 'Analyst','Admin','PermissionAdmin' )
      UNION
      /*
 * Check if the user is a part of super user list.
 */
      SELECT '__SUPER_USER__',
                   -1,
                   7
        FROM super_users
         WHERE USER_ID = v_userId
                 AND is_active = 1
      UNION
      /*
 * Check the contextual roles in this BA that have permission to view private requests.
 */
      SELECT CASE rolename
                                WHEN 'Logger' THEN '__LOGGER_PRIVATE__'
                                WHEN 'Assignee' THEN '__ASSIGNEE_PRIVATE__'
                                WHEN 'Subscriber' THEN '__SUBSCRIBER_PRIVATE__'
                   ELSE rolename
                      END,
                   -1,
                   gpermissions
        FROM roles_permissions rp
               JOIN roles r
                ON rp.sys_id = r.sys_id
               AND rp.role_id = r.role_id
         WHERE rp.sys_id = v_systemId
                 AND rp.field_id = v_privateFieldId
                 AND r.rolename IN ( 'Logger','Assignee','Subscriber' );

   /*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpr ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GPBYUSERID" /*stp_roleperm_getPermissionsByUserId*/
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_prefixList IN VARCHAR2 DEFAULT NULL
)
AS
   v_privateFieldId NUMBER(10,0);
   v_query VARCHAR2(7999);
BEGIN
   SELECT NVL(field_id, 0)
     INTO v_privateFieldId
     FROM fields
      WHERE NAME = 'is_private';

   v_query := '
   CREATE TABLE #tmp
   (
        mailListId INT
   )

   /*
    * Get the mailing lists where the user is a direct member.
    */
   INSERT INTO #tmp(mailListId)
   select
        mail_list_id
   from
        mail_list_users
   where
        user_id = ' ||to_char(v_userId) || '

   SELECT * INTO #tmp1 FROM #tmp
   WHILE (EXISTS(SELECT * FROM #tmp1))
   BEGIN
        /*
         * Get the mailing lists where the id in #tmp1 is a member.
            * which is already not part of #tmp
         */
        SELECT
             mlu.mail_list_id
        INTO #tmp2
        FROM
             mail_list_users mlu
             JOIN #tmp1 t1
             ON mlu.user_id = t1.mailListId
             LEFT JOIN #tmp t
             ON mlu.mail_list_id = t.mailListId
        WHERE
             t.mailListId IS NULL

        INSERT INTO #tmp1 SELECT * FROM #tmp2
        INSERT INTO #tmp SELECT * FROM #tmp2
        DROP TABLE #tmp2
        DELETE #tmp1
   END
   DROP TABLE #tmp1

   SELECT
        ba.sys_prefix ''SysPrefix'',
        f.sys_id ''SystemId'',
        f.field_id ''FieldId'',
        f.name ''FieldName'',
        case sum(CONVERT(INT, padd))
             when 0 then 0
             else 1
        end
        +
        case sum(CONVERT(INT, pchange))
             when 0 then 0
             else 2
        end
        +
        case sum(CONVERT(INT, pview))
             when 0 then 0
             else 4
        end ''Permission''
   FROM
        fields f
        JOIN business_areas ba
        ON f.sys_id = ba.sys_id
        LEFT JOIN roles_permissions rp
        ON rp.sys_id = f.sys_id AND rp.field_id = f.field_id AND f.is_active = 1
        LEFT JOIN roles_users ru
        ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id AND (ru.user_id = ' || to_char(v_userId) || ' OR ru.user_id in (select mailListID from #tmp)) AND ru.is_active = 1
        JOIN permissions p
        ON rp.gpermissions = p.permission
   WHERE
        ba.sys_prefix in (''' || v_prefixList || ''') AND
        f.is_extended = 0 AND
        f.is_active = 1 AND
        (
             rp.role_id IN (1) OR
             rp.role_id = ru.role_id
        )
   group by ba.sys_prefix, f.sys_id, f.name, f.field_id
   UNION
   SELECT
        ba.sys_prefix ''SysPrefix'',
        rp.sys_id ''SystemId'',
        -1 ''FieldId'',
        ''__LOGGER_PRIVATE__'' ''FieldName'',
        case (gpermissions )
          when 0 then 0
          else 4
        end ''Permission''
   FROM
        roles_permissions rp
        JOIN business_areas ba
        ON rp.sys_id = ba.sys_id
   WHERE
        ba.sys_prefix in (''' || v_prefixList || ''') AND
        role_id = 2 AND -- LOGGER ROLE
        field_id = ' || to_char(v_privateFieldId) || '
   UNION
   SELECT
        ba.sys_prefix ''SysPrefix'',
        rp.sys_id ''SystemId'',
        -1 ''FieldId'',
        ''__ASSIGNEE_PRIVATE__'' ''FieldName'',
        case (gpermissions )
          when 0 then 0
          else 4
        end ''Permission''
   FROM
        roles_permissions rp
        JOIN business_areas ba
        ON rp.sys_id = ba.sys_id
   WHERE
        ba.sys_prefix in (''' || v_prefixList || ''') AND
        role_id = 3 AND -- ASSIGNEE ROLE
        field_id = ' || to_char(v_privateFieldId) || '
   UNION
   SELECT
        ba.sys_prefix ''SysPrefix'',
        rp.sys_id ''SystemId'',
        -1 ''FieldId'',
        ''__SUBSCRIBER_PRIVATE__'' ''FieldName'',
        case (gpermissions )
          when 0 then 0
          else 4
        end ''Permission''
   FROM
        roles_permissions rp
        JOIN business_areas ba
        ON rp.sys_id = ba.sys_id
   WHERE
        ba.sys_prefix in (''' || v_prefixList || ''') AND
        role_id = 4 AND -- SUBSCRIBER ROLE
        field_id = ' || to_char(v_privateFieldId) || '
   ORDER by  SystemId, FieldId, FieldName, Permission

   DROP TABLE #tmp
   ';

   DBMS_OUTPUT.PUT_LINE(v_query);

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLEP_GPSIDUIDFORADDREQ" /*stp_roleperm_getPermissionsBySystemIdAndUserIdForAddRequest*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
   v_privateFieldId NUMBER(10,0);
BEGIN
   /*
 * Get the mailing lists where the user is a member directly or indirectly.
 */
   /*
 * Get the mailing lists where the user is a direct member.
 */
   INSERT INTO tt_tmpq
     ( mailListId )
     ( SELECT mail_list_id
       FROM mail_list_users
          WHERE USER_ID = v_userId );

   DELETE FROM tt_tmpq1;

   INSERT INTO tt_tmpq1 (
      SELECT *
        FROM tt_tmpq  );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE ( EXISTS ( SELECT *
                           FROM tt_tmpq1  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         DELETE FROM tt_tmpq2;

         INSERT INTO tt_tmpq2 (
            /*
      * Get the mailing lists where the id in #tmp1 is a member.
         * which is already not part of #tmp
      */
            SELECT mlu.mail_list_id
              FROM mail_list_users mlu
                     JOIN tt_tmpq1 t1
                      ON mlu.USER_ID = t1.mailListId
                     LEFT JOIN tt_tmpq t
                      ON mlu.mail_list_id = t.mailListId
               WHERE t.mailListId IS NULL );

         INSERT INTO tt_tmpq1
           ( SELECT *
             FROM tt_tmpq2  );

         INSERT INTO tt_tmpq
           ( SELECT *
             FROM tt_tmpq2  );

         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpq2 ';

         DELETE tt_tmpq1
         ;

      END;
   END LOOP;

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpq1 ';

   SELECT NVL(field_id, 0)
     INTO v_privateFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'is_private';

   OPEN cv_1 FOR
      SELECT t.NAME "name",
             t.field_id "field_id",
             CASE SUM(p.padd)
                             WHEN 0 THEN 0
             ELSE 1
                END + CASE SUM(p.pchange)
                                         WHEN 0 THEN 0
             ELSE 2
                END + CASE SUM(p.pview)
                                       WHEN 0 THEN 0
             ELSE 4
                END "permission"
        FROM PERMISSIONS p
               /*
      * Get the permissions the user gets by virtue of being a user of the system and 
      * being a logger of the request.
      */
               JOIN ( SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( rp.role_id = 1
                                 OR-- User Role.
                                  rp.role_id = 2 )-- Logger Role.
                                 
                      UNION
                      /*
      * Get the permissions the user gets by virtue of being a part of the BA.
      */
                      SELECT f.NAME,
                                   f.field_id,
                                   rp.gpermissions
                      FROM roles_permissions rp
                             JOIN fields f
                              ON rp.sys_id = f.sys_id
                             AND rp.field_id = f.field_id
                             JOIN roles_users ru
                              ON ru.sys_id = rp.sys_id
                             AND ru.role_id = rp.role_id
                         WHERE rp.sys_id = v_systemId
                                 AND ( ru.USER_ID = v_userId
                                 OR ru.USER_ID IN ( SELECT mailListId
                                                    FROM tt_tmpq  ) ) ) t
                ON p.permission = t.gpermissions
        GROUP BY t.NAME,t.field_id
      UNION
      /*
 * Get the list of application specific roles the user is present in.
 */
      SELECT CASE rolename
                                WHEN 'Analyst' THEN '__ROLE_ANALYST__'
                                WHEN 'Admin' THEN '__ADMIN__'
                                WHEN 'PermissionAdmin' THEN '__PERMISSIONADMIN__'
                   ELSE rolename
                      END,
                   -1,
                   -1
        FROM roles_users ru
               JOIN roles r
                ON r.sys_id = ru.sys_id
               AND r.role_id = ru.role_id
               AND ru.is_active = 1
         WHERE ru.sys_id = v_systemId
                 AND ( ru.USER_ID = v_userId
                 OR ru.USER_ID IN ( SELECT mailListId
                                    FROM tt_tmpq  ) )
                 AND r.rolename IN ( 'Analyst','Admin','PermissionAdmin' )
      UNION
      /*
 * Check if the user is a part of super user list.
 */
      SELECT '__SUPER_USER__',
                   -1,
                   -1
        FROM super_users
         WHERE USER_ID = v_userId
                 AND is_active = 1
      UNION
      /*
 * Check the contextual roles in this BA that have permission to view private requests.
 */
      SELECT CASE rolename
                                WHEN 'Logger' THEN '__LOGGER_PRIVATE__'
                                WHEN 'Assignee' THEN '__ASSIGNEE_PRIVATE__'
                                WHEN 'Subscriber' THEN '__SUBSCRIBER_PRIVATE__'
                   ELSE rolename
                      END,
                   -1,
                   gpermissions
        FROM roles_permissions rp
               JOIN roles r
                ON rp.sys_id = r.sys_id
               AND rp.role_id = r.role_id
         WHERE rp.sys_id = v_systemId
                 AND rp.field_id = v_privateFieldId
                 AND r.rolename IN ( 'Logger','Assignee','Subscriber' );

   /*
 * Finally drop the temp table used for holding the mailing lists this user is part of directly or indirectly.
 */
   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmpq ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLES_DELETE_" 
(
  v_role_id IN NUMBER DEFAULT 0
)
AS
BEGIN
   DELETE ROLES

      WHERE ( role_id = v_role_id );

   DELETE roles_permissions

      WHERE ( role_id = v_role_id );

   DELETE roles_users

      WHERE ( role_id = v_role_id );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLES_INSERT" 
(
  -- Add the parameters for the stored procedure here
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_rolename IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_role_id NUMBER(10,0);
BEGIN
   SELECT NVL(MAX(role_id), 0)
     INTO v_role_id
     FROM roles ;

   v_role_id := v_role_id + 1;

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_sys_id, v_role_id, v_rolename, v_description );

   OPEN cv_1 FOR
      SELECT v_role_id
        FROM DUAL ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROLES_PERMISSIONS_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_role_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_gpermissions IN NUMBER DEFAULT NULL ,
  v_dpermissions IN NUMBER DEFAULT NULL
)
AS
   v_already_defined NUMBER;
BEGIN
   v_already_defined := 0;

   SELECT COUNT(*)
     INTO v_already_defined
     FROM roles_permissions
      WHERE sys_id = v_sys_id
              AND role_id = v_role_id
              AND field_id = v_field_id;

   IF v_already_defined = 0 THEN
   BEGIN
      INSERT INTO roles_permissions
        ( sys_id, role_id, field_id, gpermissions, dpermissions )
        VALUES ( v_sys_id, v_role_id, v_field_id, v_gpermissions, v_dpermissions );

   END;
   ELSE
   BEGIN
      UPDATE roles_permissions
         SET gpermissions = v_gpermissions,
             dpermissions = v_dpermissions
         WHERE sys_id = v_sys_id
        AND role_id = v_role_id
        AND field_id = v_field_id;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_ROP_GPRIPSIDRIDAIDUIDLIST" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_userId IN VARCHAR2 DEFAULT NULL
)
AS
   v_query VARCHAR2(7999);
BEGIN
   v_query := '

   CREATE TABLE #tmp
   (
        mailListId INT,
        userId         INT
   )

   /*
    * Get the mailing lists where the user is a direct member.
    */
   INSERT INTO #tmp(mailListId,userId)
   select
        mail_list_id,user_id
   from
        mail_list_users
   where
        user_id in (' || v_userId || ')

   SELECT * INTO #tmp1 FROM #tmp
   WHILE (EXISTS(SELECT * FROM #tmp1))
   BEGIN
        /*
         * Get the mailing lists where the id in #tmp1 is a member.
            * which is already not part of #tmp
         */
        SELECT
             mlu.mail_list_id,t1.userId
        INTO #tmp2
        FROM
             mail_list_users mlu
             JOIN #tmp1 t1
             ON mlu.user_id = t1.mailListId
             LEFT JOIN #tmp t
             ON mlu.mail_list_id = t.mailListId
        WHERE
             t.mailListId IS NULL or (mlu.user_id = t1.mailListId and mlu.mail_list_id = t.mailListId)

        INSERT INTO #tmp1 SELECT * FROM #tmp2
        INSERT INTO #tmp SELECT * FROM #tmp2
        DROP TABLE #tmp2
        DELETE #tmp1
   END
   DROP TABLE #tmp1

   -- Get Private Permissions on User role

   SELECT
        f.name ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id ''userId''
        into #tmp3
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id  AND f.name=''is_private''
        LEFT JOIN users u
        ON u.user_id in (' || v_userId || ')
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char( v_sysId) || ' AND
        (
             rp.role_id = 1
        )

   -- Get Private Permissions for Roles User_Id_List user are in

   Insert into #tmp3
   SELECT
        f.name  ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id "userId"
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id AND f.name=''is_private''
        LEFT JOIN roles_users ru
        ON ru.sys_id = rp.sys_id AND ru.role_id = rp.role_id
        LEFT JOIN #tmp mlu
        ON mlu.mailListId = ru.user_id
        LEFT JOIN users u
        ON u.user_id = ru.user_id or mlu.userId = u.user_id
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char( v_sysId)  || ' AND
        (
             u.user_id in (' || v_userId || ')
        )

   -- Get Private Permissions for Action_USER roles  User_Id_List user are in

   Insert into #tmp3
   SELECT
        f.name  ''name'',
        f.field_id  ''field_id'',
        padd,
        pchange,
        pview,
        u.user_id "userId"
   FROM
        fields f
        LEFT JOIN roles_permissions rp
        ON f.sys_id = rp.sys_id AND f.field_id = rp.field_id   AND f.name=''is_private''
        LEFT JOIN action_users aq
        ON aq.sys_id = rp.sys_id AND aq.request_id = ' || to_char(v_requestId) || ' AND aq.action_id = ' || to_char(v_actionId) || '      and aq.user_type_id = rp.role_id
        LEFT JOIN #tmp mlu
        ON mlu.mailListId =aq.user_id
        LEFT JOIN users u
        ON u.user_id = aq.user_id or mlu.userId = u.user_id
        JOIN permissions pm
        ON pm.permission = rp.gpermissions
   WHERE
        f.sys_id = ' || to_char( v_sysId)  || ' AND
        (
             u.user_id in (' || v_userId || ')
        )

   select userId,
        case sum(CONVERT(INT,padd))
             when 0 then 0
             else 1
        end
        +
        case sum(CONVERT(INT, pchange))
             when 0 then 0
             else 2
        end
        +
        case sum(CONVERT(INT,pview))
             when 0 then 0
             else 4
        end  ''permission''
   from #tmp3
   GROUP BY name, field_id,userId
   drop table #tmp3
   drop table #tmp
   ';

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RU_GETALLROLEUSERS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT sys_id,
             role_id,
             USER_ID,
             is_active
        FROM roles_users ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RU_GETBAADMINLIST" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT DISTINCT u.USER_ID,
                      u.email,
                      u.user_login
        FROM roles_users ru
               JOIN users u
                ON ru.USER_ID = u.USER_ID
         WHERE ru.role_id = 9
                 AND u.is_active = 1
                 AND ru.is_active = 1
        ORDER BY email;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RU_GETUSERBALIST" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  iv_userTypeIdList IN VARCHAR2 DEFAULT NULL ,
  v_isPrimary IN NUMBER DEFAULT NULL
)
AS
   v_userTypeIdList VARCHAR2(256) := iv_userTypeIdList;
   v_query VARCHAR2(7999);
BEGIN
   IF ( v_userTypeIdList IS NULL
     OR v_userTypeIdList = '' ) THEN
   BEGIN
      v_userTypeIdList := '-1';

   END;
   END IF;

   v_query := '
   select DISTINCT sys_prefix
   FROM
        request_users ru
        JOIN business_areas ba
        ON ru.sys_id = ba.sys_id
   WHERE
        ru.user_id = ' || --sqlserver_utilities.convert('VARCHAR2(20)', v_userId)
         to_char(v_userid)|| ' AND
        (
             ru.user_type_id IN (' || v_userTypeIdList || ')';

   IF ( v_isPrimary = 1 ) THEN
   BEGIN
      v_query := v_query || ' OR
                (
                     ru.user_type_id = 3 AND
                     ru.is_primary = 1
                )
      ';

   END;
   END IF;

   v_query := v_query || '
        )
   ';

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_RU_LUPBYSIDROLEID" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_roleId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM roles_users ru
         WHERE sys_id = v_sysId
                 AND role_id = v_roleId
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_SEVERITY_ESCALATION" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_temp NUMBER(1, 0) := 0;
BEGIN
/*NOTE CHANGE IS:- mark begin to end as the comment*/
 /* BEGIN
      select 1 INTO v_temp
        FROM DUAL
       WHERE NOT EXISTS ( SELECT 1
                          FROM TABLES
                             WHERE TABLE_TYPE = 'BASE TABLE'
                                     AND TABLE_NAME = 'escalation_history' );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;*/

   -- SET NOCOUNT ON added to prevent extra result sets from
   -- interfering with SELECT statements.
   -- new Due date can be in the following format +2mon +1wk +30d +2hours -5m
   -- First param of DateDiff need to be changed based on our unit of time. It can be :
   --     Datepart      Abbreviations
   --     Year      yy, yyyy
   --     quarter      qq, q
   --     Month      mm, m
   --     dayofyear      dy, y
   --     Day      dd, d
   --     Week      wk, ww
   --     Hour      hh
   --     minute      mi, n
   --     second      ss, s
   --     millisecond      ms
   IF v_temp = 1 THEN
   NULL;
   END IF;

   DELETE FROM tt_esctmp;

   INSERT INTO tt_esctmp (
      SELECT r.sys_id sys_id,
             r.request_id request_id,
             ec.span timespan,
             ru.USER_ID cur_assignee,
             eh.parent_user_id new_assignee_id,
             u.user_login new_assignee
        FROM requests r
               LEFT JOIN escalation_history hist
                ON r.request_id = hist.request_id
               AND hist.sys_id = r.sys_id
               JOIN escalation_conditions ec
                ON r.sys_id = ec.sys_id
               AND ec.severity_id = r.severity_id
               --AND sqlserver_utilities.datediff('MI', NVL(hist.last_escalated_time, sqlserver_utilities.dateadd('MI', 330, r.logged_datetime)), SYSDATE) > ec.span
               AND((hist.last_escalated_time- sysdate) > ec.span
               or (r.logged_datetime+330-sysdate)>ec.span)
               JOIN request_users ru
                ON r.sys_id = ru.sys_id
               AND user_type_id = 3
               AND r.request_id = ru.request_id
               LEFT JOIN escalation_heirarchy eh
                ON r.sys_id = eh.sys_id
               AND ru.USER_ID = eh.USER_ID
               LEFT JOIN users u
                ON u.USER_ID = eh.parent_user_id
         WHERE r.status_id != 3 );

   OPEN cv_1 FOR
      SELECT t1.*
        FROM tt_esctmp t1
         WHERE t1.new_assignee_id NOT IN ( SELECT cur_assignee
                                           FROM tt_esctmp t2
                                              WHERE t2.sys_id = t1.sys_id
                                                      AND t2.request_id = t1.request_id );

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_esctmp ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_ADVANCEDACTONGROUP" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  iv_requestList IN VARCHAR2 DEFAULT NULL ,
  v_isPrivate IN VARCHAR2 DEFAULT NULL ,
  iv_category IN VARCHAR2 DEFAULT NULL ,
  iv_status IN VARCHAR2 DEFAULT NULL ,
  iv_severity IN VARCHAR2 DEFAULT NULL ,
  iv_requestType IN VARCHAR2 DEFAULT NULL ,
  v_description IN VARCHAR2 DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_rejectedList OUT VARCHAR2
)
AS
   v_category VARCHAR2(255) := iv_category;
   v_status VARCHAR2(255) := iv_status;
   v_severity VARCHAR2(255) := iv_severity;
   v_requestType VARCHAR2(255) := iv_requestType;
   v_requestList VARCHAR2(7999) := iv_requestList;
   v_catFieldId NUMBER(10,0);
   v_statFieldId NUMBER(10,0);
   v_sevFieldId NUMBER(10,0);
   v_reqTypeFieldId NUMBER(10,0);
   v_catFieldName VARCHAR2(255);
   v_statFieldName VARCHAR2(255);
   v_sevFieldName VARCHAR2(255);
   v_reqTypeFieldName VARCHAR2(255);
   v_confFieldId NUMBER(10,0);
   v_categoryId NUMBER(10,0);
   v_statusId NUMBER(10,0);
   v_severityId NUMBER(10,0);
   v_requestTypeId NUMBER(10,0);
   v_isPrivateValue NUMBER(10,0);
   v_index NUMBER(10,0);
   v_requestId NUMBER(10,0);
BEGIN
   v_rejectedList := '';

   -- Get the Field ID of status. and typeId of closed.
   SELECT field_id,
                display_name
     INTO v_catFieldId,
          v_catFieldName
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'category_id';

   SELECT field_id,
          display_name
     INTO v_statFieldId,
          v_statFieldName
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'status_id';

   SELECT field_id,
          display_name
     INTO v_sevFieldId,
          v_sevFieldName
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'severity_id';

   SELECT field_id,
          display_name
     INTO v_reqTypeFieldId,
          v_reqTypeFieldName
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'request_type_id';

   SELECT field_id
     INTO v_confFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'is_private';

   --- Trim the input params
   v_category := LTRIM(RTRIM(v_category));

   v_status := LTRIM(RTRIM(v_status));

   v_severity := LTRIM(RTRIM(v_severity));

   v_requestType := LTRIM(RTRIM(v_requestType));

   -- Get the corresponding type ids if they are not empty.
   v_categoryId := -1;

   v_statusId := -1;

   v_severityId := -1;

   v_requestTypeId := -1;

   --- Get the Category.
   IF ( v_category != '' ) THEN
   BEGIN
      SELECT NVL(TYPE_ID, -1),
             NVL(display_name, v_category)
        INTO v_categoryId,
             v_category
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_catFieldId
                 AND ( NAME = v_category
                 OR display_name = v_category );

   END;
   END IF;

   --- Get the status.
   IF ( v_status != '' ) THEN
   BEGIN
      SELECT NVL(TYPE_ID, -1),
             NVL(display_name, v_status)
        INTO v_statusId,
             v_status
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_statFieldId
                 AND ( NAME = v_status
                 OR display_name = v_status );

   END;
   END IF;

   --- Get the Severity.
   IF ( v_severity != '' ) THEN
   BEGIN
      SELECT NVL(TYPE_ID, -1),
             NVL(display_name, v_severity)
        INTO v_severityId,
             v_severity
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_sevFieldId
                 AND ( NAME = v_severity
                 OR display_name = v_severity );

   END;
   END IF;

   --- Get the Request Type.
   IF ( v_requestType != '' ) THEN
   BEGIN
      SELECT NVL(TYPE_ID, -1),
             NVL(display_name, v_requestType)
        INTO v_requestTypeId,
             v_requestType
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_reqTypeFieldId
                 AND ( NAME = v_requestType
                 OR display_name = v_requestType );

   END;
   END IF;

   v_isPrivateValue := CASE v_isPrivate
                                       WHEN 'none' THEN -1
                                       WHEN 'private' THEN 1
                                       WHEN 'public' THEN 0
   ELSE -1
      END;

   DBMS_OUTPUT.PUT_LINE('
   Category     : ' || to_char( v_categoryId) ||' 
   Status       : ' || to_char(v_statusId) || '
   Severity      : ' || to_char(v_severityId) ||' 
   RequestType     :  '|| to_char(v_requestTypeId) || '
   Private          : ' || to_char(v_isPrivateValue));

   IF ( v_categoryId = -1
     AND v_statusId = -1
     AND v_severityId = -1
     AND v_requestTypeId = -1
     AND v_isPrivateValue = -1 ) THEN
   BEGIN
      v_rejectedList := v_requestList;

      RETURN;

   END;
   END IF;

   <<loop_1>>
   WHILE ( v_requestList <> '' )
   LOOP
      DECLARE
         /*
        Get the following information of this request:
          1. MaxActionId
          2. Current Category Id
          3. Current Status Id
          4. Current Severity Id
          5. Current Request Type Id.
          6. Current Private Value.
      */
         v_maxActionId NUMBER(10,0);
         v_curCatId NUMBER(10,0);
         v_curStatId NUMBER(10,0);
         v_curSevId NUMBER(10,0);
         v_curReqTypeId NUMBER(10,0);
         v_curPrivateValue NUMBER(1,0);
         v_curCatName VARCHAR2(255);
         v_curStatName VARCHAR2(255);
         v_curSevName VARCHAR2(255);
         v_curReqTypeName VARCHAR2(255);
         v_actionId NUMBER(10,0);
         v_headerDesc VARCHAR2(1000);
         v_continue NUMBER(1,0);
      BEGIN
         v_index := INSTR(v_requestList, ',');

         IF ( v_index > 0 ) THEN
         BEGIN
            v_requestId := to_number(v_requestList);    
             v_requestList := SUBSTR(v_requestList, v_index + 1, LENGTH(v_requestList));

         END;
         ELSE
         BEGIN
            v_requestId := to_number(v_requestList);

            v_requestList := '';

         END;
         END IF;

         v_continue := 0;

         SELECT category_id,
                status_id,
                severity_id,
                request_type_id,
                is_private,
                max_action_id
           INTO v_curCatId,
                v_curStatId,
                v_curSevId,
                v_curReqTypeId,
                v_curPrivateValue,
                v_maxActionId
           FROM requests
            WHERE sys_id = v_systemId
                    AND request_id = v_requestId;

         -- Increment the max_action_id to get the actionid.
         v_actionId := v_maxActionId + 1;

         -- Generate the header description.
         v_headerDesc := '';

         IF ( v_categoryId != -1
           AND v_categoryId <> v_curCatId ) THEN
         BEGIN
            SELECT display_name
              INTO v_curCatName
              FROM types
               WHERE sys_id = v_systemId
                       AND field_id = v_catFieldId
                       AND TYPE_ID = v_curCatId;

            v_headerDesc := v_headerDesc || 'category_id##' || to_char(v_catFieldId) || '##[ ' || v_catFieldName || ' changed from ''' || v_curCatName || ''' to ''' || v_category || ''' ] ' || CHR(10);

            v_continue := 1;

         END;
         END IF;

         IF ( v_statusId != -1
           AND v_statusId <> v_curStatId ) THEN
         BEGIN
            SELECT display_name
              INTO v_curStatName
              FROM types
               WHERE sys_id = v_systemId
                       AND field_id = v_statFieldId
                       AND TYPE_ID = v_curStatId;

            IF ( v_status = 'closed' ) THEN
            BEGIN
               v_headerDesc := v_headerDesc || 'status_id##' || to_char(v_statFieldId) || '##[ Closed ] ' || CHR(10);

            END;
            ELSE
            BEGIN
               v_headerDesc := v_headerDesc || 'status_id##' ||to_char(v_statFieldId) || '##[ ' || v_statFieldName || ' changed from ''' || v_curStatName || ''' to ''' || v_status || ''' ] ' || CHR(10);

            END;
            END IF;

            v_continue := 1;

         END;
         END IF;

         IF ( v_severityId != -1
           AND v_severityId <> v_curSevId ) THEN
         BEGIN
            SELECT display_name
              INTO v_curSevName
              FROM types
               WHERE sys_id = v_systemId
                       AND field_id = v_sevFieldId
                       AND TYPE_ID = v_curSevId;

            v_headerDesc := v_headerDesc || 'severity_id##' || to_char(v_sevFieldId) || '##[ ' || v_sevFieldName || ' changed from ''' || v_curSevName || ''' to ''' || v_severity || ''' ] ' || CHR(10);

            v_continue := 1;

         END;
         END IF;

         IF ( v_requestTypeId != -1
           AND v_requestTypeId <> v_curReqTypeId ) THEN
         BEGIN
            SELECT display_name
              INTO v_curReqTypeName
              FROM types
               WHERE sys_id = v_systemId
                       AND field_id = v_reqTypeFieldId
                       AND TYPE_ID = v_curReqTypeId;

            v_headerDesc := v_headerDesc || 'request_type_id##' || to_char(v_reqTypeFieldId) || '##[ ' || v_reqTypeFieldName || ' changed from ''' || v_curReqTypeName || ''' to ''' || v_requestType || ''' ] ' || CHR(10);

            v_continue := 1;

         END;
         END IF;

         IF ( v_isPrivateValue != -1
           AND v_isPrivateValue <> v_curPrivateValue ) THEN
         BEGIN
            IF ( v_isPrivateValue = 1 ) THEN
            BEGIN
               v_headerDesc := v_headerDesc || 'is_private##' ||to_char(v_confFieldId) || '##[ Marked private ]' || CHR(10);

               v_continue := 1;

            END;
            END IF;

            IF ( v_isPrivateValue = 0 ) THEN
            BEGIN
               v_headerDesc := v_headerDesc || 'is_private##' ||to_char(v_confFieldId) || '##[ Marked public ]' || CHR(10);

               v_continue := 1;

            END;
            END IF;

         END;
         END IF;

         -- If there is no change in any of the above five fields, there is no point in just appending.
         -- So, add this request to the rejected list and continue.
         IF ( v_continue = 0 ) THEN
         BEGIN
            IF ( v_rejectedList = '' ) THEN
            BEGIN
               v_rejectedList := to_char(v_requestId);

            END;
            ELSE
            BEGIN
               v_rejectedList := v_rejectedList || ',' || to_char(v_requestId);

            END;
            END IF;

            GOTO loop_1;

         END;
         END IF;

         -- Continue with the next request id.
         v_headerDesc := v_headerDesc || '[ No email notification ] ' || CHR(10);

         -- Update the request
         UPDATE requests
            SET category_id = CASE v_categoryId
                                               WHEN -1 THEN category_id
                ELSE v_categoryId
                   END,
                status_id = CASE v_statusId
                                           WHEN -1 THEN status_id
                ELSE v_statusId
                   END,
                severity_id = CASE v_severityId
                                               WHEN -1 THEN severity_id
                ELSE v_severityId
                   END,
                request_type_id = CASE v_requestTypeId
                                                      WHEN -1 THEN request_type_id
                ELSE v_requestTypeId
                   END,
                is_private = CASE v_isPrivateValue
                                                  WHEN -1 THEN is_private
                ELSE v_isPrivateValue
                   END,
                DESCRIPTION = v_description,
                USER_ID = v_userId,
                lastupdated_datetime = SYS_EXTRACT_UTC(SYSTIMESTAMP),
                max_action_id = v_actionId
            WHERE sys_id = v_systemId
           AND request_id = v_requestId;

         -- Insert the corresponding actions records
         INSERT INTO actions
           ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action )
           ( SELECT sys_id,
                    request_id,
                    v_actionId,
                    CASE v_categoryId
                                     WHEN -1 THEN category_id
                    ELSE v_categoryId
                       END,
                    CASE v_statusId
                                   WHEN -1 THEN status_id
                    ELSE v_statusId
                       END,
                    CASE v_severityId
                                     WHEN -1 THEN severity_id
                    ELSE v_severityId
                       END,
                    CASE v_requestTypeId
                                        WHEN -1 THEN request_type_id
                    ELSE v_requestTypeId
                       END,
                    SUBJECT,
                    v_description,
                    CASE v_isPrivateValue
                                         WHEN -1 THEN is_private
                    ELSE v_isPrivateValue
                       END,
                    parent_request_id,
                    v_userId,
                    due_datetime,
                    logged_datetime,
                    SYS_EXTRACT_UTC(SYSTIMESTAMP),
                    v_headerDesc,
                    '',
                    summary,
                    memo,
                    append_interface,
                    0,
                    0,
                    0
             FROM actions
                WHERE sys_id = v_systemId
                        AND request_id = v_requestId
                        AND action_id = v_maxActionId );

         -- Insert the corresponding actions_ex records
         INSERT INTO actions_ex
           ( sys_id, request_id, action_id, field_id, bit_value, datetime_value, int_value, real_value, varchar_value, text_value, type_value )
           ( SELECT sys_id,
                    request_id,
                    v_actionId,
                    field_id,
                    bit_value,
                    datetime_value,
                    int_value,
                    real_value,
                    varchar_value,
                    text_value,
                    type_value
             FROM actions_ex
                WHERE sys_id = v_systemId
                        AND request_id = v_requestId
                        AND action_id = v_maxActionId );

      END;
   END LOOP;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_CHANGEREADSTATUS" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  iv_requestList IN VARCHAR2 DEFAULT NULL ,
  v_action IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_rejectedList OUT VARCHAR2
)
AS
   v_requestList VARCHAR2(7999) := iv_requestList;
   v_index NUMBER(10,0);
   v_requestId NUMBER(10,0);
   v_actionId NUMBER(10,0);
   v_GA_READ NUMBER(10,0);
   v_GA_UNREAD NUMBER(10,0);
BEGIN
   v_GA_READ := 4;

   v_GA_UNREAD := 5;

   v_rejectedList := '';

   WHILE ( v_requestList <> '' )
   LOOP
      BEGIN
         v_index := INSTR(v_requestList, ',');

         IF ( v_index > 0 ) THEN
         BEGIN
            v_requestId := to_number(v_requestList);

            v_requestList := SUBSTR(v_requestList, v_index + 1, LENGTH(v_requestList));

         END;
         ELSE
         BEGIN
            v_requestId := to_number( v_requestList);

            v_requestList := '';

         END;
         END IF;

         IF ( v_action = v_GA_READ ) THEN
         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            -- Get the Max ActionId
            SELECT max_action_id
              INTO v_actionId
              FROM requests
               WHERE sys_id = v_systemId
                       AND request_id = v_requestId;

            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE EXISTS ( SELECT sys_id,
                                      request_id,
                                      USER_ID
                               FROM user_read_actions
                                  WHERE sys_id = v_systemId
                                          AND request_id = v_requestId
                                          AND USER_ID = v_userId );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               /*
                * Update the record with max_action_id for this request, if it is already existing.
                */
               UPDATE user_read_actions
                  SET action_id = v_actionId
                  WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND USER_ID = v_userId;

            END;
            ELSE
            BEGIN
               /*
                * Insert a new record.
                */
               INSERT INTO user_read_actions
                 ( sys_id, request_id, action_id, USER_ID )
                 VALUES ( v_systemId, v_requestId, v_actionId, v_userId );

            END;
            END IF;

         END;
         ELSE
            IF ( v_action = v_GA_UNREAD ) THEN
            BEGIN
               /*
           * User wants to mark this request as unread, delete the record.
           */
               DELETE user_read_actions

                  WHERE sys_id = v_systemId
                          AND request_id = v_requestId
                          AND USER_ID = v_userId;

            END;
            END IF;

         END IF;

      END;
   END LOOP;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_CREATEBUSINESSAREA" 
(
  v_sysName IN VARCHAR2 DEFAULT NULL ,
  v_sysPrefix IN VARCHAR2 DEFAULT NULL
)
AS
   v_systemId NUMBER(10,0);
   v_userId NUMBER(10,0);
BEGIN
   SELECT USER_ID
     INTO v_userId
     FROM users
      WHERE user_login = 'root';

   SELECT NVL(MAX(sys_id), 0)
     INTO v_systemId
     FROM business_areas ;

   v_systemId := v_systemId + 1;

   -- Insert the Business Area Record.
   INSERT INTO business_areas
     ( sys_id, NAME, display_name, email, sys_prefix, DESCRIPTION, TYPE, location, max_request_id, max_email_actions, is_email_active, is_active, date_created, is_private, sys_config, field_config )
     VALUES ( v_systemId, v_sysName, v_sysName, v_sysprefix || '@localhost', v_sysPrefix, v_sysName, 'Development', 'hyd', 0, 10, 1, 1, SYS_EXTRACT_UTC(SYSTIMESTAMP), 0, '
        <SysConfig>
            <!-- Default CSS to be used for this business area. -->
            <Stylesheet web="tbits.css" email="null" />
             <!-- Datetime related options. -->
            <DefaultDueDate allowNull="true" duration="0" />
            <DateFormat list="6" email="6" />
             <!-- Mailing options. -->
            <Notify request="1" action="1" />
            <NotifyLogger request="true" action="true" />
            <MailFormat format="1" />
            <!-- Severity related options. -->
            <Severity>
                <Incoming highValue="critical" lowValue="low" />
                <Outgoing highValue="critical" lowValue="low" />
             </Severity>
             <!-- Other options.-->
             <Assign all="false" volunteer="0" />
             <LegacyPrefixes list="" />
             <!-- Custom links if any. -->
             <CustomLinks>
             </CustomLinks>
        </SysConfig>
        ', NULL );

   --- Insert the Field Records.
   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 1, 'sys_id', 'Business Area', 'BA', 5, 0, 0, 1, '', 6, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 2, 'request_id', 'Request', 'Request', 5, 0, 0, 1, '', 47, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 3, 'category_id', 'Category', 'Category', 9, 0, 3, 1, '', 254, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 4, 'status_id', 'Status', 'Status', 9, 0, 3, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 5, 'severity_id', 'Priority', 'Severity', 9, 0, 3, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 6, 'request_type_id', 'Request Type', 'Request Type', 9, 0, 3, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 7, 'logger_ids', 'Logger', 'Loggers', 10, 0, 3, 1, '', 191, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 8, 'assignee_ids', 'Assignee', 'Assignees', 10, 0, 3, 1, '', 191, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 9, 'subscriber_ids', 'Subscribers', 'Subscribers', 10, 0, 5, 1, '', 63, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 10, 'to_ids', 'To', 'To', 10, 0, 2, 1, '', 125, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 11, 'cc_ids', 'Cc', 'CC', 10, 0, 2, 1, '', 125, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 12, 'subject', 'Subject', 'Subject', 7, 0, 3, 1, '', 127, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 13, 'description', 'Description', 'Description', 8, 0, 0, 1, '', 103, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 14, 'is_private', 'Private', 'Private', 1, 0, 1, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 15, 'parent_request_id', 'Parent', 'Parent', 5, 0, 3, 1, '', 127, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 16, 'user_id', 'Last Update By.', 'User', 10, 0, 0, 1, '', 44, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 17, 'max_action_id', '# U', 'Action', 5, 0, 0, 1, '', 44, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 18, 'due_datetime', 'Due Date', 'Due', 4, 0, 3, 1, '', 127, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 19, 'logged_datetime', 'Submitted Date', 'Logged', 4, 0, 0, 1, '', 44, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 20, 'lastupdated_datetime', 'Last Updated', 'Last Updated', 4, 0, 0, 1, '', 44, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 21, 'header_description', 'Header Description', 'Header', 8, 0, 0, 1, '', 4, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 22, 'attachments', 'Attachments', 'Attach', 8, 0, 0, 1, '', 103, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 23, 'summary', 'Summary', 'Summary', 8, 0, 1, 1, '', 103, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 24, 'memo', 'Memo', 'Memo', 8, 0, 1, 1, '', 7, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 25, 'append_interface', 'append_interface', 'append_interface', 5, 0, 0, 1, '', 0, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 26, 'notify', 'Notify', 'Notify', 1, 0, 1, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 27, 'notify_loggers', 'Notify Logger', 'Notify Logger', 1, 0, 1, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 28, 'replied_to_action', 'replied_to_action', 'replied_to_action', 5, 0, 1, 1, '', 70, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 29, 'related_requests', 'Linked Requests', 'Linked Requests', 7, 0, 4, 1, '', 23, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_extended, tracking_option, is_active, regex, permission, is_private )
     VALUES ( v_systemId, 30, 'office_id', 'Office', 'Office', 9, 0, 3, 1, '', 126, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group )
     VALUES ( v_systemId, 31, 'SendSMS', 'Send SMS', 'Sends SMS', 1, 1, 1, 0, 0, 47, '', 0, 0, 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent, display_order, display_group )
     VALUES ( v_systemId, 32, 'sms_id', 'SMS Id', 'The Id of SMS format', 5, 1, 1, 0, 0, 0, '', 0, 0, 0 );

   --- Insert into field_descriptor table
   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 1, 'ba', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 2, 'req', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 2, 'request', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 3, 'cat', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 3, 'category', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 4, 'stat', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 4, 'status', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 5, 'sev', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 5, 'severity', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 6, 'reqtype', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 6, 'requesttype', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 6, 'type', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 7, 'log', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 7, 'logger', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 7, 'loggers', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'ass', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'assignee', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'assignees', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'assign', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 9, 'sub', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 9, 'subscriber', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 9, 'subscribers', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 10, 'to', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 11, 'cc', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 12, 'subj', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 13, 'desc', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 13, 'alltext', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 14, 'conf', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 15, 'par', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 15, 'parent', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 16, 'user', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 17, 'action', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'ddate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'due', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'dueby', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'duedate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 19, 'ldate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 19, 'loggeddate', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 20, 'udate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 20, 'updateddate', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 21, 'hdr', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 22, 'att', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 23, 'sum', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 23, 'summary', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 24, 'memo', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 25, 'actfrm', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 26, 'notify', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 26, 'mail', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 27, 'notlog', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 28, 'reAction', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 29, 'relReq', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 29, 'link', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 29, 'linked', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 30, 'off', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 31, 'sendsms', 1 );

   ---- Insert the default Types.
   ---- Categories
   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 3, 1, 'pending', 'Others', 'Pending - Not specified or Misc', 1, 1, 1, 1, 0, 0 );

   ---- Statuses
   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 4, 1, 'Pending', 'Pending', 'Pending', 1, 1, 1, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 4, 2, 'Active', 'Active', 'Active', 2, 1, 0, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 4, 3, 'Closed', 'Closed', 'Closed', 3, 1, 0, 0, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 4, 4, 'Reopened', 'Reopened', 'Reopened', 4, 1, 0, 0, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 4, 5, 'Suspended', 'Suspended', 'Suspended', 5, 1, 0, 0, 0, 0 );

   ---- Severities
   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 5, 1, 'low', 'Low', 'Low', 1, 1, 0, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 5, 2, 'medium', 'Medium', 'Medium', 2, 1, 1, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 5, 3, 'high', 'High', 'High', 3, 1, 0, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 5, 4, 'critical', 'Critical', 'Critical', 4, 1, 0, 1, 0, 0 );

   ---- Request Types.
   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 6, 1, 'request', 'Request', 'Request', 1, 1, 1, 1, 0, 0 );

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 6, 2, 'question', 'Question', 'Question', 2, 1, 0, 1, 0, 0 );

   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES(@systemId, 6, 3, 'countermeasure', 'Counter Measure', 'Counter Measure', 3, 1, 0, 1, 0, 0)
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES(@systemId, 6, 4, 'manual', 'Manual', 'Manual', 4, 1, 0, 1, 0, 0)
   ----Insert default locations
   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_systemId, 30, 1, 'default', '-', 'default', 1, 1, 1, 1, 0, 0 );

   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 2, 'Gurgaon Plant', 'Gurgaon Plant', 'Houston', 2, 1, 0, 0, 0, 0)
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 3, 'Hyderabad', 'HYD', 'Hyderabad', 3, 1, 0, 0, 0, 0)
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 4, 'Kansas City', 'KC', 'Kansas City', 4, 1, 0, 0, 0, 0)
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 5, 'London', 'LON', 'London', 5, 1, 0, 0, 0, 0 )
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 6, 'New York', 'NYC', 'New York', 6, 1, 0, 0, 0, 0)
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES ( @systemId, 30, 7, 'San Fransisco', 'SF', 'San Fransisco', 7, 1, 0, 0, 0, 0)
   --
   --
   --INSERT INTO types (sys_id, field_id, type_id, name, display_name, description, ordering, is_active, is_default, is_checked, is_private, is_final)
   --VALUES (@systemId, 30, 8, 'Silicon Valley', 'SV', 'Silicon Valley', 8, 1, 0, 0, 0, 0)
   ---- INSERT INTO roles.
   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 1, 'User', 'User' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 2, 'Logger', 'Logger' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 3, 'Assignee', 'Assignee' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 4, 'Subscriber', 'Subscriber' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 5, 'To', 'To' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 6, 'Cc', 'Cc' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 7, 'Analyst', 'Analyst' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 8, 'Manager', 'Manager' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 9, 'Admin', 'Admin' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 10, 'PermissionAdmin', 'PermissionAdmin' );

   --Insert one BAUser
   --INSERT INTO business_area_users(sys_id,user_id) VALUES(@systemId, @userId)
   stp_business_area_users_insert(v_systemId,
                                        v_userId,
                                        1);

   /**
Permission Values.
===================
|   Type | Value. |
=================== 
|    Add |  1     |
| Change |  2     |
|   View |  4     |
=================== 
*/
   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 1, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 2, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 3, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 4, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 5, 6, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 6, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 7, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 8, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 9, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 10, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 11, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 12, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 13, 5, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 14, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 15, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 16, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 18, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 19, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 20, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 21, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 22, 5, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 23, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 24, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 26, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 27, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 29, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 30, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 1, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 2, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 3, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 4, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 5, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 6, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 7, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 8, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 9, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 10, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 11, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 12, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 13, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 14, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 15, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 16, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 18, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 19, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 20, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 21, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 22, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 23, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 24, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 26, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 27, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 29, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 30, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 1, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 2, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 3, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 4, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 5, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 6, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 7, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 8, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 9, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 10, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 11, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 12, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 13, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 14, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 15, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 16, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 18, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 19, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 20, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 21, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 22, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 23, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 24, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 26, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 27, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 29, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 30, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 1, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 2, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 3, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 4, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 5, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 6, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 7, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 8, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 9, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 10, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 11, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 12, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 13, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 14, 4, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 15, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 16, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 18, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 19, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 20, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 21, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 22, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 23, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 24, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 26, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 27, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 29, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 30, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 1, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 2, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 3, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 4, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 5, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 6, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 7, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 8, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 9, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 10, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 11, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 12, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 13, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 14, 7, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 15, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 16, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 18, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 19, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 20, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 21, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 22, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 23, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 24, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 26, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 27, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 29, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 30, 3, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 1, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 2, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 3, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 4, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 5, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 6, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 7, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 8, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 9, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 10, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 11, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 12, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 13, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 14, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 15, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 16, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 17, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 18, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 19, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 20, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 21, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 22, 2, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 23, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 24, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 25, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 26, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 27, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 28, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 29, 0, 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 30, 0, 0 );

   STP_TBITS_INSEAZATDEFAULTS(v_systemId);

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_DELETEBUSINESSAREA" 
(
  v_systemId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DBMS_OUTPUT.PUT_LINE('Deleting Exclusion List:');

   DELETE exclusion_list

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Request Extended Fields:');

   DELETE requests_ex

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Request Users:');

   DELETE request_users

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Requests:');

   DELETE requests

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Action Extended Fields:');

   DELETE actions_ex

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Action Users:');

   DELETE action_users

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Actions:');

   DELETE actions

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Role-Users:');

   DELETE roles_users

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Role-Permissions:');

   DELETE roles_permissions

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Roles:');

   DELETE roles

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting BA Users:');

   DELETE business_area_users

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Type Users:');

   DELETE type_users

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Types:');

   DELETE types

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Field Descriptors:');

   DELETE field_descriptors

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting Fields:');

   DELETE fields

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting BA Record:');

   DELETE business_areas

      WHERE sys_id = v_systemId;

   DBMS_OUTPUT.PUT_LINE('Deleting post process rules ');

   DELETE post_process_rules

      WHERE sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_GETCOMPLETEACTION" 
/*CREATE OR REPLACE PROCEDURE stp_tbits_getCompleteAction*/
(
  v_sysPrefix IN VARCHAR2 DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_userLogin IN VARCHAR2 DEFAULT NULL ,
  v_actionOrder IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR,
  cv_5 IN OUT SYS_REFCURSOR,
  cv_6 IN OUT SYS_REFCURSOR
)
AS
   v_systemId NUMBER(10,0);
   v_privateFieldId NUMBER(10,0);
   v_userId NUMBER(10,0);
BEGIN
   v_userId := 0;

   SELECT USER_ID
     INTO v_userId
     FROM users
      WHERE user_login = v_userLogin;

   /*
 * Check if the USER ID is valid in TBits user database.
 */
   IF ( v_userId IS NULL
     OR v_userId = 0 ) THEN
   BEGIN
      OPEN cv_1 FOR
         SELECT *
           FROM requests
            WHERE 1 = 2;

   END;
   END IF;

   v_systemId := 0;

   SELECT sys_id
     INTO v_systemId
     FROM business_areas
      WHERE sys_prefix = v_sysPrefix;

   DBMS_OUTPUT.PUT_LINE(v_systemId);

   /*
 * Check if the BA ID is valid in tBits database.
 */
   IF ( v_systemId IS NULL
     OR v_systemId = 0 ) THEN
   BEGIN
      OPEN cv_2 FOR
         SELECT *
           FROM requests
            WHERE 1 = 2;

   END;
   END IF;

   -- Get the Field ID of the is_private field.
   SELECT field_id
     INTO v_privateFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'is_private';

   OPEN cv_3 FOR
      SELECT req.sys_id,
             req.request_id,
             ba.sys_prefix "sys_prefix",
             req.SUBJECT,
             --(req.is_private||(cat.is_private||ba.is_private)) "is_private",
              bitor(req.is_private,bitor(cat.is_private,ba.is_private)) "is_private",
             cat.display_name "category",
             stat.display_name "status",
             sev.display_name "severity",
             rt.display_name "request_type",
             req.logged_datetime,
             req.lastupdated_datetime,
             req.due_datetime
        FROM business_areas ba
               JOIN requests req
                ON ba.sys_id = req.sys_id
               JOIN types cat
                ON cat.sys_id = req.sys_id
               AND cat.field_id = 3
               AND cat.TYPE_ID = req.category_id
               JOIN types stat
                ON stat.sys_id = req.sys_id
               AND stat.TYPE_ID = req.status_id
               AND stat.field_id = 4
               JOIN types sev
                ON sev.sys_id = req.sys_id
               AND sev.TYPE_ID = req.severity_id
               AND sev.field_id = 5
               JOIN types rt
                ON rt.sys_id = req.sys_id
               AND rt.TYPE_ID = req.request_type_id
               AND rt.field_id = 6
         WHERE ba.sys_id = v_systemId
                 AND request_id = v_requestId;

   OPEN cv_4 FOR
      SELECT ru.user_type_id,
             u.display_name,
             u.user_login
        FROM request_users ru
               JOIN users u
                ON ru.USER_ID = u.USER_ID
         WHERE ru.sys_id = v_systemId
                 AND ru.request_id = v_requestId;

   OPEN cv_5 FOR
      SELECT CASE SUM(p.pview)
                     WHEN 0 THEN 0
             ELSE 4
                END "permission"
        FROM ( SELECT DISTINCT bitor(rup.gpermissions,rqp.gpermissions) permission
               /*
                          * Join with the mail_list_users to consider if the group the user is present in
                          * has any association with the business area or the request.
                          */
               /*
                          * Get the permissions the user has by virtue of his association with the
                          * business area.
                          */
               /*
                          * Get the permissions the user has by virtue of his association with the
                          * request.
                          */
               FROM fields f
                      LEFT JOIN mail_list_users mlu
                       ON mlu.USER_ID = v_userId
                      JOIN roles_permissions rup
                       ON f.sys_id = rup.sys_id
                      AND f.field_id = rup.field_id
                      LEFT JOIN roles_users ru
                       ON rup.sys_id = ru.sys_id
                      AND rup.role_id = ru.role_id
                      AND ru.is_active = 1
                      JOIN roles_permissions rqp
                       ON f.sys_id = rqp.sys_id
                      AND f.field_id = rqp.field_id
                      LEFT JOIN request_users rq
                       ON rqp.sys_id = rq.sys_id
                      AND rqp.role_id = rq.user_type_id
                      AND rq.request_id = v_requestId
                  WHERE f.sys_id = v_systemId
                          AND f.field_id = v_privateFieldId
                          AND ( rup.role_id = 1
                          OR ( ru.USER_ID IS NOT NULL
                          AND ( ru.USER_ID = v_userId
                          OR ru.USER_ID = mlu.mail_list_id ) ) )
                          AND ( rqp.role_id = 1
                          OR ( rq.USER_ID IS NOT NULL
                          AND ( rq.USER_ID = v_userId
                          OR rq.USER_ID = mlu.mail_list_id ) ) ) 
               ) a
               JOIN PERMISSIONS p
                ON a.permission = p.permission;

   IF ( v_actionOrder = 0 ) THEN
   BEGIN
      OPEN cv_6 FOR
         SELECT action_id,
                request_id,
                is_private,
                sys_id,
                u.display_name,
                u.user_login,
                lastupdated_datetime,
                DESCRIPTION,
                header_description
           FROM actions a
                  JOIN users u
                   ON a.USER_ID = u.USER_ID
            WHERE a.sys_id = v_systemId
                    AND a.request_id = v_requestId
           ORDER BY action_id ASC;

   END;
   ELSE
   BEGIN
      OPEN cv_6 FOR
         SELECT action_id,
                request_id,
                is_private,
                sys_id,
                u.display_name,
                u.user_login,
                lastupdated_datetime,
                DESCRIPTION,
                header_description
           FROM actions a
                  JOIN users u
                   ON a.USER_ID = u.USER_ID
            WHERE a.sys_id = v_systemId
                    AND a.request_id = v_requestId
           ORDER BY action_id DESC;

   END;
   END IF;

END

;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_GREQINFOINRANGE" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_start IN NUMBER DEFAULT NULL ,
  v_end IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR,
  cv_2 IN OUT SYS_REFCURSOR,
  cv_3 IN OUT SYS_REFCURSOR,
  cv_4 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      /*
 * Return the request records.
 */
      /*
         * Boolean Fields.
         */
      SELECT DISTINCT r.is_private,
                            r.notify,
                            r.notify_loggers,
                            /*
         * Date Fields.
         */
                            r.logged_datetime,
                            r.lastupdated_datetime,
                            r.due_datetime,
                            /*
         * Integer Fields.
         */
                            r.request_id,
                            r.parent_request_id,
                            r.max_action_id,
                            r.append_interface,
                            r.replied_to_action,
                            /*
         * String Fields.
         */
                            ba.sys_prefix "sys_prefix",
                            r.SUBJECT,
                            /*
      * Type fields. Take the name and display name.
      * User can search on any of them.
      */
                            cat.NAME || ' ' || cat.display_name "category_id",
                            stat.NAME || ' ' || stat.display_name "status_id",
                            sev.NAME || ' ' || sev.display_name "severity_id",
                            req.NAME || ' ' || req.display_name "request_type_id",
                            office.NAME || ' ' || office.display_name "office_id",
                            /*
         * User Fields.
         */
                            usr.user_login "user_id"
        FROM requests r
               LEFT JOIN business_areas ba
                ON ba.sys_id = r.sys_id
               LEFT JOIN types cat
                ON cat.sys_id = r.sys_id
               AND cat.field_id = 3
               AND cat.TYPE_ID = r.category_id
               LEFT JOIN types stat
                ON stat.sys_id = r.sys_id
               AND stat.field_id = 4
               AND stat.TYPE_ID = r.status_id
               LEFT JOIN types sev
                ON sev.sys_id = r.sys_id
               AND sev.field_id = 5
               AND sev.TYPE_ID = r.severity_id
               LEFT JOIN types req
                ON req.sys_id = r.sys_id
               AND req.field_id = 6
               AND req.TYPE_ID = r.request_type_id
               LEFT JOIN types office
                ON req.sys_id = r.sys_id
               AND office.field_id = 30
               AND office.TYPE_ID = r.office_id
               LEFT JOIN users usr
                ON usr.USER_ID = r.USER_ID
         WHERE r.sys_id = v_systemId
                 AND r.request_id >= v_start
                 AND r.request_id < v_end
        ORDER BY r.request_id;

   OPEN cv_2 FOR
      /*
 * Send out the request user details.
 */
      SELECT r.request_id,
                   r.user_type_id,
                   usr.user_login,
                   r.is_primary
        FROM request_users r
               JOIN users usr
                ON usr.USER_ID = r.USER_ID
         WHERE r.sys_id = v_systemId
                 AND r.request_id >= v_start
                 AND r.request_id < v_end
        ORDER BY r.request_id,
                 r.user_type_id,
                 user_login;

   OPEN cv_3 FOR
      /*
 * Send the requests ex details.
 */
      SELECT r.request_id,
                   f.NAME "field_name",
                   f.data_type_id,
                   r.bit_value,
                   r.datetime_value,
                   r.int_value,
                   r.real_value,
                   r.varchar_value,
                   r.text_value,
                   NVL(t.NAME, '') || ' ' || NVL(t.display_name, '') "type_value"
        FROM requests_ex r
               JOIN fields f
                ON r.sys_id = f.sys_id
               AND r.field_id = f.field_id
               LEFT JOIN types t
                ON r.sys_id = t.sys_id
               AND r.field_id = t.field_id
               AND r.type_value = t.TYPE_ID
         WHERE r.sys_id = v_systemId
                 AND r.request_id >= v_start
                 AND r.request_id < v_end
        ORDER BY r.request_id;

   OPEN cv_4 FOR
      /*
 * Return the action records
 */
      SELECT a.request_id,
                   a.action_id,
                   stat.NAME "status_id",
                   a.DESCRIPTION,
                   a.summary,
                   a.attachments,
                   usr.user_login "user_id",
                   a.lastupdated_datetime
        FROM actions a
               JOIN types stat
                ON stat.sys_id = a.sys_id
               AND stat.field_id = 4
               AND stat.TYPE_ID = a.status_id
               JOIN users usr
                ON usr.USER_ID = a.USER_ID
         WHERE a.sys_id = v_systemId
                 AND a.request_id >= v_start
                 AND a.request_id < v_end
        ORDER BY a.request_id,
                 a.action_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_INSEAZATDEFAULTS" 
(
  v_systemId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   /*
 * Delete standard roles.
 */
   DELETE roles

      WHERE sys_id = v_systemId;

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 1, 'User', 'User' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 2, 'Logger', 'Logger' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 3, 'Assignee', 'Assignee' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 4, 'Subscriber', 'Subscriber' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 5, 'Cc', 'Cc' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 6, 'To', 'To' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 7, 'Analyst', 'Analyst' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 8, 'Manager', 'Manager' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 9, 'Admin', 'Admin' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 10, 'PermissionAdmin', 'Permission Admin' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 11, 'Customer', 'Customer' );

   INSERT INTO roles
     ( sys_id, role_id, rolename, DESCRIPTION )
     VALUES ( v_systemId, 12, 'BAUsers', 'BAUsers' );

   /*
 * Delete permissions standard roles have on standard fields.
 */
   DELETE roles_permissions

      WHERE sys_id = v_systemId
              AND field_id IN ( SELECT field_id
                                FROM fields
                                   WHERE sys_id = v_systemId
                                           AND is_extended = 0 );

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 1, 0, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 2, 6, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 3, 4, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 4, 4, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 5, 4, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 6, 4, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 7, 4, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 8, 4, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 9, 6, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 10, 7, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 11, 7, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 12, 4, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 13, 5, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 15, 7, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 16, 4, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 17, 4, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 18, 4, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 19, 4, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 20, 4, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 21, 4, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 22, 5, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 23, 4, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 24, 7, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 26, 4, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 27, 4, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 28, 4, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 29, 4, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 31, 7, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 1, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 1, 0, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 2, 1, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 7, 2, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 9, 1, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 11, 1, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 12, 3, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 14, 6, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 23, 3, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 2, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 3, 2, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 4, 2, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 5, 2, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 6, 2, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 8, 2, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 9, 2, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 12, 2, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 14, 6, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 18, 2, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 23, 2, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 26, 2, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 27, 2, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 29, 2, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 3, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 9, 0, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 12, 0, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 14, 6, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 23, 0, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 4, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 2, 7, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 3, 4, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 4, 4, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 5, 7, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 6, 4, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 7, 4, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 8, 4, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 9, 7, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 10, 7, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 11, 7, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 12, 4, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 13, 5, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 15, 7, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 16, 4, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 17, 4, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 18, 4, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 19, 4, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 20, 4, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 21, 4, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 22, 5, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 23, 4, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 24, 7, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 26, 4, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 27, 4, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 28, 4, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 29, 7, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 5, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 2, 7, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 3, 4, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 4, 4, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 5, 7, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 6, 4, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 7, 4, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 8, 4, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 9, 7, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 10, 7, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 11, 7, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 12, 4, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 13, 5, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 15, 7, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 16, 4, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 17, 4, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 18, 4, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 19, 4, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 20, 4, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 21, 4, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 22, 5, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 23, 4, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 24, 7, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 26, 4, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 27, 4, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 28, 4, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 29, 7, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 6, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 1, 0, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 9, 0, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 12, 0, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 23, 0, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 7, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 1, 0, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 9, 0, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 12, 0, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 23, 0, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 8, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 3, 2, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 5, 2, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 6, 2, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 8, 3, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 9, 2, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 12, 2, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 14, 6, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 18, 2, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 23, 2, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 26, 2, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 27, 2, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 29, 2, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 9, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 2, 7, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 3, 7, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 4, 7, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 5, 7, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 6, 7, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 7, 7, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 8, 7, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 9, 7, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 10, 7, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 11, 7, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 12, 7, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 13, 5, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 14, 6, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 15, 7, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 16, 4, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 17, 4, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 18, 7, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 19, 4, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 20, 4, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 21, 4, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 22, 5, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 23, 7, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 24, 7, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 26, 7, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 27, 7, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 28, 4, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 29, 7, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 30, 7, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 10, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 1, 0, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 9, 0, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 12, 0, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 23, 0, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 11, 32, 0, 0 );--SMS_ID
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 1, 4, 0 );--BusinessArea
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 2, 0, 0 );--Request
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 3, 0, 0 );--Category
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 4, 0, 0 );--Status
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 5, 0, 0 );--Severity
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 6, 0, 0 );--RequestType
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 7, 0, 0 );--Logger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 8, 0, 0 );--Assignee
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 9, 0, 0 );--Subscribers
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 10, 0, 0 );--To
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 11, 0, 0 );--Cc
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 12, 0, 0 );--Subject
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 13, 0, 0 );--Description
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 14, 0, 0 );--Private
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 15, 0, 0 );--Parent
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 16, 0, 0 );--LastUpdateBy
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 17, 0, 0 );--#Updates
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 18, 0, 0 );--DueDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 19, 0, 0 );--LoggedDate
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 20, 0, 0 );--LastUpdated
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 21, 0, 0 );--HeaderDescription
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 22, 0, 0 );--Attachments
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 23, 0, 0 );--Summary
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 24, 0, 0 );--Memo
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 25, 0, 0 );--AppendInterface
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 26, 0, 0 );--Notify
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 27, 0, 0 );--NotifyLogger
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 28, 0, 0 );--RepliedToAction
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 29, 0, 0 );--LinkedRequests
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 30, 0, 0 );--Office
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 31, 0, 0 );--SendSMS
     

   INSERT INTO roles_permissions
     ( sys_id, role_id, field_id, gpermissions, dpermissions )
     VALUES ( v_systemId, 12, 32, 0, 0 );--SMS_ID
     

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_INSERTSDFDDEFAULTS" 
(
  v_systemId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   /*
 * DELETE standard fields if any for this business area and insert them again.
 */
   DELETE fields

      WHERE sys_id = v_systemId
              AND is_extended = 0;

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 1, 'sys_id', 'Business Area', 'sys_id', 5, 1, 0, 0, 0, 6, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 2, 'request_id', 'Request', 'Request', 5, 1, 0, 0, 0, 47, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 3, 'category_id', 'Category', 'category_id', 9, 1, 0, 0, 3, 254, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 4, 'status_id', 'Status', 'status_id', 9, 1, 0, 0, 3, 126, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 5, 'severity_id', 'Severity', 'severity_id', 9, 1, 0, 0, 3, 126, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 6, 'request_type_id', 'Request Type', 'request_type_id', 9, 1, 0, 0, 3, 126, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 7, 'logger_ids', 'Logger', 'logger_ids', 10, 1, 0, 0, 3, 191, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 8, 'assignee_ids', 'Assignee', 'assignee_ids', 10, 1, 0, 0, 3, 191, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 9, 'subscriber_ids', 'Subscribers', 'subscriber_ids', 10, 1, 0, 0, 5, 63, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 10, 'to_ids', 'To', 'to_ids', 10, 1, 0, 0, 2, 125, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 11, 'cc_ids', 'Cc', 'cc_ids', 10, 1, 0, 0, 2, 125, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 12, 'subject', 'Subject', 'summary', 7, 1, 0, 0, 3, 127, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 13, 'description', 'Description', 'description', 8, 1, 0, 0, 0, 103, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 14, 'is_private', 'Private', 'is_private', 1, 1, 0, 0, 3, 118, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 15, 'parent_request_id', 'Parent', 'parent_request_id', 5, 1, 0, 0, 3, 127, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 16, 'user_id', 'Last Update By', 'user_id', 10, 1, 0, 0, 0, 44, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 17, 'max_action_id', '# Updates', 'max_action_id', 5, 1, 0, 0, 0, 44, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 18, 'due_datetime', 'Due Date', 'due_datetime', 4, 1, 0, 0, 3, 127, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 19, 'logged_datetime', 'Logged Date', 'logged_datetime', 4, 1, 0, 0, 0, 44, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 20, 'lastupdated_datetime', 'Last Updated', 'lastupdated_datetime', 4, 1, 0, 0, 0, 44, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 21, 'header_description', 'Header Description', 'header_description', 8, 1, 0, 0, 0, 4, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 22, 'attachments', 'Attachments', 'attachments', 8, 1, 0, 0, 0, 103, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 23, 'summary', 'Summary', 'Summary', 8, 1, 0, 0, 1, 103, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 24, 'memo', 'Memo', 'Memo', 8, 1, 0, 0, 0, 7, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 25, 'append_interface', 'Append Interface', 'append_interface', 5, 1, 0, 0, 0, 0, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 26, 'notify', 'Notify', 'notify', 1, 1, 0, 0, 1, 126, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 27, 'notify_loggers', 'Notify Logger', 'notify_loggers', 1, 1, 0, 0, 1, 126, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 28, 'replied_to_action', 'Replied To Action', 'replied_to_action', 5, 1, 0, 0, 0, 70, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 29, 'related_requests', 'Linked Requests', 'related_requests', 7, 1, 0, 0, 4, 23, '', 0 );

   INSERT INTO fields
     ( sys_id, field_id, NAME, display_name, DESCRIPTION, data_type_id, is_active, is_extended, is_private, tracking_option, permission, regex, is_dependent )
     VALUES ( v_systemId, 30, 'office_id', 'Office', 'Office', 9, 1, 0, 0, 3, 126, '', 0 );

   /*
 * DELETE field descriptors of standard fields if any and insert them again.
 */
   DELETE field_descriptors

      WHERE sys_id = v_systemId
              AND field_id IN ( SELECT field_id
                                FROM fields
                                   WHERE sys_id = v_systemId
                                           AND is_extended = 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 1, 'ba', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 2, 'req', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 3, 'cat', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 4, 'stat', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 5, 'sev', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 6, 'reqtype', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 6, 'type', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 7, 'log', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'ass', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 8, 'assignee', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 9, 'sub', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 10, 'to', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 11, 'cc', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 12, 'subj', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 13, 'alltext', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 13, 'desc', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 14, 'conf', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 14, 'private', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 15, 'par', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 16, 'user', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 17, 'updates', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'ddate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'due', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'dueby', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 18, 'duedate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 19, 'ldate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 19, 'loggeddate', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 20, 'udate', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 20, 'updateddate', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 22, 'att', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 23, 'sum', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 23, 'summary', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 26, 'mail', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 26, 'notify', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 27, 'notlog', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 29, 'link', 1 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 29, 'relreq', 0 );

   INSERT INTO field_descriptors
     ( sys_id, field_id, field_descriptor, is_primary )
     VALUES ( v_systemId, 30, 'off', 1 );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_INSERTSFIIDDEFWRAP" 
(
  v_systemId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   STP_TBITS_INSERTSDFDDEFAULTS(v_systemId);

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_RENAMEBUSINESSAREA" 
(
  v_oldId IN NUMBER DEFAULT NULL ,
  v_newId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DBMS_OUTPUT.PUT_LINE('Updating Exclusion List:');

   UPDATE exclusion_list
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Request Extended Fields:');

   UPDATE requests_ex
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Request Users:');

   UPDATE request_users
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Requests:');

   UPDATE requests
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Action Extended Fields:');

   UPDATE actions_ex
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Action Users:');

   UPDATE action_users
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Actions:');

   UPDATE actions
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Role-Users:');

   UPDATE roles_users
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Role-Permissions:');

   UPDATE roles_permissions
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Roles:');

   UPDATE roles
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating BA Users:');

   UPDATE business_area_users
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Type Users:');

   UPDATE type_users
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Types:');

   UPDATE types
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating Fields:');

   UPDATE fields
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

   DBMS_OUTPUT.PUT_LINE('Updating BA Record:');

   UPDATE business_areas
      SET sys_id = v_newId
      WHERE sys_id = v_oldId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TBITS_SIMPLEACTONGROUP" /*no use in java code*/
(
  v_systemId IN NUMBER DEFAULT NULL ,
  iv_requestList IN VARCHAR2 DEFAULT NULL ,
  v_action IN NUMBER DEFAULT NULL ,
  v_description IN VARCHAR2 DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  v_rejectedList OUT VARCHAR2
)
AS
   v_requestList VARCHAR2(7999) := iv_requestList;
   v_maxActionId NUMBER(10,0);
   v_closedStatusId NUMBER(10,0);
   v_statusFieldId NUMBER(10,0);
   v_privateFieldId NUMBER(10,0);
   v_finalPrivateStatus NUMBER(10,0);
   v_isPrivate NUMBER(1,0);
   v_index NUMBER(10,0);
   v_requestId NUMBER(10,0);
   v_actionId NUMBER(10,0);
   v_statusDisplayName VARCHAR2(128);
   v_headerDesc VARCHAR2(1000);
BEGIN
   v_rejectedList := '';

   /*
 * Get the Field ID of status. and typeId of closed.
 */
   SELECT field_id
     INTO v_statusFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'status_id';

   SELECT TYPE_ID,
          display_name
     INTO v_closedStatusId,
          v_statusDisplayName
     FROM types t
      WHERE t.sys_id = v_systemId
              AND t.field_id = v_statusFieldId
              AND NAME = 'closed';

   /*
 * GET the field ID of is_private
 */
   SELECT field_id
     INTO v_privateFieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = 'is_private';

   WHILE ( v_requestList <> '' )
   LOOP
      BEGIN
         v_index := INSTR(v_requestList, ',');

         IF ( v_index > 0 ) THEN
         BEGIN
            v_requestId := sqlserver_utilities.convert('NUMBER(10,0)', SUBSTR(v_requestList, 0, v_index));

            v_requestList := SUBSTR(v_requestList, v_index + 1, LENGTH(v_requestList));

         END;
         ELSE
         BEGIN
           v_requestId := sqlserver_utilities.convert('NUMBER(10,0)', v_requestList);

            v_requestList := '';

         END;
         END IF;

         -- Get the Max ActionId
         SELECT max_action_id
           INTO v_maxActionId
           FROM requests
            WHERE sys_id = v_systemId
                    AND request_id = v_requestId;

         IF ( v_action = 1 ) THEN
         DECLARE
            v_temp NUMBER(1, 0) := 0;
         BEGIN
            v_headerDesc := 'status_id##' || sqlserver_utilities.convert('VARCHAR2(4000)', v_statusFieldId) || '##[ ' || v_statusDisplayName || ' ]' || CHR(10) || '[ No e-mail notification ]';

            BEGIN
               SELECT 1 INTO v_temp
                 FROM DUAL
                WHERE ( ( SELECT status_id
                          FROM requests
                             WHERE sys_id = v_systemId
                                     AND request_id = v_requestId ) = v_closedStatusId );
            EXCEPTION
               WHEN OTHERS THEN
                  NULL;
            END;

            IF v_temp = 1 THEN
            BEGIN
               IF ( v_rejectedList IS NOT NULL
                 AND v_rejectedList != '' ) THEN
               BEGIN
                  v_rejectedList := v_rejectedList || ',';

               END;
               END IF;

              v_rejectedList := v_rejectedList || sqlserver_utilities.convert('VARCHAR2(100)', v_requestId);

            END;
            ELSE
            BEGIN
               -- Update the Status for this request.
               v_actionId := v_maxActionId + 1;

               UPDATE requests
                  SET status_id = v_closedStatusId,
                      DESCRIPTION = v_description,
                      USER_ID = v_userId,
                      max_action_id = v_actionId,
                      lastupdated_datetime = SYS_EXTRACT_UTC(SYSTIMESTAMP),
                      attachments = ''
                  WHERE sys_id = v_systemId
                 AND request_id = v_requestId;

               INSERT INTO actions
                 ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action )
                 ( SELECT sys_id,
                          request_id,
                          v_actionId,
                          category_id,
                          v_closedStatusId,
                          severity_id,
                          request_type_id,
                          SUBJECT,
                          v_description,
                          is_private,
                          parent_request_id,
                          v_userId,
                          due_datetime,
                          logged_datetime,
                          SYS_EXTRACT_UTC(SYSTIMESTAMP),
                          v_headerDesc,
                          '',
                          summary,
                          memo,
                          append_interface,
                          0,
                          0,
                          0
                   FROM actions
                      WHERE sys_id = v_systemId
                              AND request_id = v_requestId
                              AND action_id = v_maxActionId );

               -- Insert the corresponding actions_ex records
               INSERT INTO actions_ex
                 ( sys_id, request_id, action_id, field_id, bit_value, datetime_value, int_value, real_value, varchar_value, text_value, type_value )
                 ( SELECT sys_id,
                          request_id,
                          v_actionId,
                          field_id,
                          bit_value,
                          datetime_value,
                          int_value,
                          real_value,
                          varchar_value,
                          text_value,
                          type_value
                   FROM actions_ex
                      WHERE sys_id = v_systemId
                              AND request_id = v_requestId
                              AND action_id = v_maxActionId );

              DBMS_OUTPUT.PUT_LINE('Closing the request: ' || sqlserver_utilities.convert('VARCHAR2(4000)', v_requestId));

            END;
            END IF;

         END;
         ELSE
            IF ( v_action = 2
              OR v_action = 3 ) THEN
            DECLARE
               v_temp NUMBER(1, 0) := 0;
            BEGIN
               IF ( v_action = 2 ) THEN
               BEGIN
                  v_finalPrivateStatus := 1;

                  v_headerDesc := 'is_private##' || sqlserver_utilities.convert('VARCHAR2(4000)', v_privateFieldId) || '##[ Marked Private ]' || CHR(10) || '[ No e-mail notification ]';

               END;
               END IF;

               IF ( v_action = 3 ) THEN
               BEGIN
                  v_finalPrivateStatus := 0;

                  v_headerDesc := 'is_private##' || sqlserver_utilities.convert('VARCHAR2(4000)', v_privateFieldId) || '##[ Marked Public ]' || CHR(10) || '[ No e-mail notification ]';

               END;
               END IF;

               BEGIN
                  SELECT 1 INTO v_temp
                    FROM DUAL
                   WHERE ( ( SELECT is_private
                             FROM requests
                                WHERE sys_id = v_systemId
                                        AND request_id = v_requestId ) = v_finalPrivateStatus );
               EXCEPTION
                  WHEN OTHERS THEN
                     NULL;
               END;

               IF v_temp = 1 THEN
               BEGIN
                  IF ( v_rejectedList IS NOT NULL
                    AND v_rejectedList != '' ) THEN
                  BEGIN
                     v_rejectedList := v_rejectedList || ',';

                  END;
                  END IF;

                 v_rejectedList := v_rejectedList || sqlserver_utilities.convert('VARCHAR2(100)', v_requestId);

               END;
               ELSE
               BEGIN
                  -- Update the is_private for this request.
                  v_actionId := v_maxActionId + 1;

                  UPDATE requests
                     SET is_private = v_finalPrivateStatus,
                         DESCRIPTION = v_description,
                         USER_ID = v_userId,
                         max_action_id = v_actionId,
                         lastupdated_datetime = SYS_EXTRACT_UTC(SYSTIMESTAMP),
                         attachments = ''
                     WHERE sys_id = v_systemId
                    AND request_id = v_requestId;

                  INSERT INTO actions
                    ( sys_id, request_id, action_id, category_id, status_id, severity_id, request_type_id, SUBJECT, DESCRIPTION, is_private, parent_request_id, USER_ID, due_datetime, logged_datetime, lastupdated_datetime, header_description, attachments, summary, memo, append_interface, notify, notify_loggers, replied_to_action )
                    ( SELECT sys_id,
                             request_id,
                             v_actionId,
                             category_id,
                             status_id,
                             severity_id,
                             request_type_id,
                             SUBJECT,
                             v_description,
                             v_finalPrivateStatus,
                             parent_request_id,
                             v_userId,
                             due_datetime,
                             logged_datetime,
                             SYS_EXTRACT_UTC(SYSTIMESTAMP),
                             v_headerDesc,
                             '',
                             summary,
                             memo,
                             append_interface,
                             0,
                             0,
                             0
                      FROM actions
                         WHERE sys_id = v_systemId
                                 AND request_id = v_requestId
                                 AND action_id = v_maxActionId );

                  -- Insert the corresponding actions_ex records
                  INSERT INTO actions_ex
                    ( sys_id, request_id, action_id, field_id, bit_value, datetime_value, int_value, real_value, varchar_value, text_value, type_value )
                    ( SELECT sys_id,
                             request_id,
                             v_actionId,
                             field_id,
                             bit_value,
                             datetime_value,
                             int_value,
                             real_value,
                             varchar_value,
                             text_value,
                             type_value
                      FROM actions_ex
                         WHERE sys_id = v_systemId
                                 AND request_id = v_requestId
                                 AND action_id = v_maxActionId );

               END;
               END IF;

            END;
            END IF;

         END IF;

      END;
   END LOOP;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_type_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE type_descriptors

      WHERE sys_id = v_sys_id
              AND field_id = v_field_id
              AND TYPE_ID = v_type_id
              AND type_descriptor = v_type_descriptor;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_GEPRIMDESFIDTYPEID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM type_descriptors
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId
                 AND is_primary = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_GETALLTYPEDESCRIPTORS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT t.sys_id,
             t.field_id,
             t.TYPE_ID,
             NVL(td.type_descriptor, t.NAME) "type_descriptor",
             NVL(td.is_primary, 0) "is_primary"
        FROM fields f
               JOIN types t
                ON f.sys_id = t.sys_id
               AND f.field_id = t.field_id
               LEFT JOIN type_descriptors td
                ON t.sys_id = td.sys_id
               AND t.field_id = td.field_id
               AND t.TYPE_ID = td.TYPE_ID
        ORDER BY t.sys_id,
                 t.field_id,
                 t.TYPE_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_GTYDESSIDFIDIDTYPEID" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT t.sys_id,
             t.field_id,
             t.TYPE_ID,
             NVL(td.type_descriptor, t.display_name) "type_descriptor",
             NVL(td.is_primary, 0) "is_primary"
        FROM types t
               LEFT JOIN type_descriptors td
                ON t.sys_id = td.sys_id
               AND t.field_id = td.field_id
               AND t.TYPE_ID = td.TYPE_ID
         WHERE t.sys_id = v_sys_id
                 AND t.field_id = v_field_id
                 AND t.TYPE_ID = v_type_id
        ORDER BY t.sys_id,
                 t.field_id,
                 t.TYPE_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_type_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO type_descriptors
     ( sys_id, field_id, TYPE_ID, type_descriptor, is_primary )
     VALUES ( v_sys_id, v_field_id, v_type_id, v_type_descriptor, v_is_primary );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_LOOKUPBYSYSTEMID" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT t.sys_id,
             t.field_id,
             t.TYPE_ID,
             NVL(td.type_descriptor, t.NAME) "type_descriptor",
             NVL(td.is_primary, 0) "is_primary"
        FROM fields f
               JOIN types t
                ON f.sys_id = t.sys_id
               AND f.field_id = t.field_id
               LEFT JOIN type_descriptors td
                ON t.sys_id = td.sys_id
               AND t.field_id = td.field_id
               AND t.TYPE_ID = td.TYPE_ID
         WHERE f.sys_id = v_sysId
        ORDER BY t.sys_id,
                 t.field_id,
                 t.TYPE_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TD_LUPSYIDFIDTYPEDESTOR" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_fieldd IN NUMBER DEFAULT NULL ,
  v_typeDesc IN NVARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT t.*
        FROM fields f
               LEFT JOIN types t
                ON f.sys_id = t.sys_id
               AND f.field_id = t.field_id
               LEFT JOIN type_descriptors td
                ON t.sys_id = td.sys_id
               AND t.field_id = td.field_id
         WHERE t.sys_id = v_sysId
                 AND ( td.type_descriptor = v_typeDesc
                 OR t.NAME = v_typeDesc
                 OR t.display_name = v_typeDesc )
        ORDER BY t.sys_id,
                 t.field_id,
                 t.TYPE_ID;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TR_INSERT" 
(
  v_srcPrefix IN VARCHAR2 DEFAULT NULL ,
  v_srcRequestId IN NUMBER DEFAULT NULL ,
  v_tarPrefix IN VARCHAR2 DEFAULT NULL ,
  v_tarRequestId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO transferred_requests
     ( source_prefix, source_request_id, target_prefix, target_request_id )
     VALUES ( v_srcPrefix, v_srcRequestId, v_tarPrefix, v_tarRequestId );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TR_LKUPSOUPRREQUESTID" 
(
  v_srcPrefix IN VARCHAR2 DEFAULT NULL ,
  v_srcRequestId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM transferred_requests
         WHERE source_prefix = v_srcPrefix
                 AND source_request_id = v_srcRequestId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TR_UPDATE" 
(
  v_srcPrefix IN VARCHAR2 DEFAULT NULL ,
  v_srcRequestId IN NUMBER DEFAULT NULL ,
  v_tarPrefix IN VARCHAR2 DEFAULT NULL ,
  v_tarRequestId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE transferred_requests
      SET target_prefix = v_tarPrefix,
          target_request_id = v_tarRequestId
      WHERE source_prefix = v_srcPrefix
     AND source_request_id = v_srcRequestId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TU_GETANALYSTINFO" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT t.sys_id "sys_id",
             u.user_login "user_login",
             t.display_name "type_name",
             t.is_private "is_private",
             nr.display_name "email_option",
             tu.is_volunteer "is_volunteer"
        FROM types t
               LEFT JOIN type_users tu
                ON t.sys_id = tu.sys_id
               AND t.field_id = tu.field_id
               AND t.TYPE_ID = tu.TYPE_ID
               AND tu.USER_ID = v_userId
               LEFT JOIN notification_rules nr
                ON tu.notification_id = nr.notification_id
               LEFT JOIN users u
                ON tu.USER_ID = u.USER_ID
               AND tu.is_active = 1
         WHERE t.sys_id = v_systemId
                 AND t.field_id = v_fieldId
        ORDER BY t.ordering;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TU_UPDATENEXTVOLUNTEER" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   -- Clear the bit set for the previous rr_volunteer.
   UPDATE type_users
      SET rr_volunteer = 0
      WHERE sys_id = v_systemId
     AND field_id = v_fieldId
     AND TYPE_ID = v_typeId
     AND rr_volunteer = 1;

   --- Set the given user as the next rr_volunteer.
   UPDATE type_users
      SET rr_volunteer = 1
      WHERE sys_id = v_systemId
     AND field_id = v_fieldId
     AND TYPE_ID = v_typeId
     AND USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TY_LUPBYSIDFNAMETYPEID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_fieldId NUMBER(10,0);
BEGIN
   SELECT NVL(field_id, 0)
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = v_fieldName;

   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TY_LUPBYSIDFNAMETYPENAME" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  v_typeName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_fieldId NUMBER(10,0);
BEGIN
   SELECT NVL(field_id, 0)
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND ( NAME = v_fieldName
              OR display_name = v_fieldName );

   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND ( NAME = v_typeName
                 OR display_name = v_typeName );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_ordering IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_default IN NUMBER DEFAULT NULL ,
  v_is_checked IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_is_final IN NUMBER DEFAULT NULL ,
  v_returnValue OUT NUMBER
)
AS
   v_fieldName VARCHAR2(255);
   v_Delete NUMBER(10,0);
BEGIN
   SELECT NAME
     INTO v_fieldName
     FROM fields
      WHERE field_id = v_field_id;

   v_Delete := 0;

   IF ( v_field_id < 29 ) THEN
   BEGIN
      IF ( v_fieldName = 'category_id' ) THEN
      DECLARE
         v_temp NUMBER(1, 0) := 0;
      BEGIN
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE NOT EXISTS ( SELECT *
                                FROM actions a
                                   WHERE a.sys_id = v_sys_id
                                           AND a.category_id = v_type_id );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp = 1 THEN
         BEGIN
            v_Delete := 1;

         END;
         END IF;

      END;
      END IF;

      IF ( v_fieldName = 'status_id' ) THEN
      DECLARE
         v_temp NUMBER(1, 0) := 0;
      BEGIN
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE NOT EXISTS ( SELECT *
                                FROM actions a
                                   WHERE a.sys_id = v_sys_id
                                           AND a.status_id = v_type_id );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp = 1 THEN
         BEGIN
            v_Delete := 1;

         END;
         END IF;

      END;
      END IF;

      IF ( v_fieldName = 'severity_id' ) THEN
      DECLARE
         v_temp NUMBER(1, 0) := 0;
      BEGIN
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE NOT EXISTS ( SELECT *
                                FROM actions a
                                   WHERE a.sys_id = v_sys_id
                                           AND a.severity_id = v_type_id );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp = 1 THEN
         BEGIN
            v_Delete := 1;

         END;
         END IF;

      END;
      END IF;

      IF ( v_fieldName = 'request_type_id' ) THEN
      DECLARE
         v_temp NUMBER(1, 0) := 0;
      BEGIN
         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE NOT EXISTS ( SELECT *
                                FROM actions a
                                   WHERE a.sys_id = v_sys_id
                                           AND a.request_type_id = v_type_id );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp = 1 THEN
         BEGIN
            v_Delete := 1;

         END;
         END IF;

      END;
      END IF;

   END;
   END IF;

   IF ( v_field_id > 29 ) THEN
   DECLARE
      v_temp NUMBER(1, 0) := 0;
   BEGIN
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE NOT EXISTS ( SELECT *
                             FROM actions_ex
                                WHERE sys_id = v_sys_id
                                        AND field_id = v_field_id
                                        AND type_value = v_type_id );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp = 1 THEN
      BEGIN
         v_Delete := 1;

      END;
      END IF;

   END;
   END IF;

   IF ( v_Delete = 1 ) THEN
   BEGIN
      DELETE types

         WHERE sys_id = v_sys_id
                 AND field_id = v_field_id
                 AND TYPE_ID = v_type_id;

      IF ( v_is_default = 1 ) THEN
      DECLARE
         v_totalCount NUMBER(10,0);
         v_temp NUMBER(1, 0) := 0;
      BEGIN
         SELECT ( SELECT COUNT(*)
                  FROM types
                     WHERE sys_id = v_sys_id
                             AND field_id = v_field_id )
           INTO v_totalCount
           FROM DUAL ;

         BEGIN
            SELECT 1 INTO v_temp
              FROM DUAL
             WHERE ( v_totalCount = ( SELECT COUNT(*)
                                      FROM types
                                         WHERE sys_id = v_sys_id
                                                 AND field_id = v_field_id
                                                 AND is_default = 0 ) );
         EXCEPTION
            WHEN OTHERS THEN
               NULL;
         END;

         IF v_temp = 1 THEN
         BEGIN
            UPDATE types
               SET is_default = 1
               WHERE sys_id = v_sys_id
              AND field_id = v_field_id
              AND TYPE_ID = ( SELECT MIN(types.TYPE_ID)
                              FROM types
                                 WHERE types.sys_id = v_sys_id
                                         AND types.field_id = v_field_id );

         END;
         END IF;

      END;
      END IF;

      v_returnValue := 1;

   END;
   ELSE
   BEGIN
      v_returnValue := 0;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_DESCRIPTOR_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_type_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE type_descriptors

      WHERE sys_id = v_sys_id
              AND field_id = v_field_id
              AND TYPE_ID = v_type_id
              AND type_descriptor = v_type_descriptor
              AND is_primary = v_is_primary;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_DESCRIPTOR_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_type_descriptor IN NVARCHAR2 DEFAULT NULL ,
  v_is_primary IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO type_descriptors
     ( sys_id, field_id, TYPE_ID, type_descriptor, is_primary )
     VALUES ( v_sys_id, v_field_id, v_type_id, v_type_descriptor, v_is_primary );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_GDEFTSIDFID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_GDEFTSIDFNAME" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_fieldId NUMBER(10,0);
BEGIN
   SELECT field_id
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = v_fieldName;

   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_GDSTATUSSEVERITIES" 
(
  v_sysIdList IN VARCHAR2 DEFAULT NULL
)
AS
   v_query VARCHAR2(7999);
BEGIN
   v_query := '
   SELECT DISTINCT
        name,
        display_name
   FROM
        types
   WHERE
        sys_id IN (' || v_sysIdList || ') AND
        field_id = 4 AND
        is_private = 0
   ORDER BY name
   SELECT DISTINCT
        name,
        display_name
   FROM
        types
   WHERE
        sys_id IN (' || v_sysIdList || ') AND
        field_id = 5 AND
        is_private = 0
   ORDER BY name
   ';

   EXECUTE IMMEDIATE v_query;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_GETALLTYPES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM types ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id OUT NUMBER,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  iv_ordering IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_default IN NUMBER DEFAULT NULL ,
  v_is_checked IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_is_final IN NUMBER DEFAULT NULL
)
AS
   v_ordering NUMBER(10,0) := iv_ordering;
   v_typeID NUMBER(10,0);
   v_orderingID NUMBER(10,0);
BEGIN
   /*
 * Get the maximum type id for this field.
 */
   SELECT (NVL(MAX(TYPE_ID), 0) + 1)
     INTO v_typeID
     FROM types
      WHERE sys_id = v_sys_id
              AND field_id = v_field_id;

   IF ( v_ordering = 0 ) THEN
   BEGIN
      SELECT (NVL(MAX(TYPE_ID), 0) + 1)
        INTO v_ordering
        FROM types
         WHERE sys_id = v_sys_id
                 AND field_id = v_field_id;

   END;
   END IF;

   INSERT INTO types
     ( sys_id, field_id, TYPE_ID, NAME, display_name, DESCRIPTION, ordering, is_active, is_default, is_checked, is_private, is_final )
     VALUES ( v_sys_id, v_field_id, v_typeID, v_name, v_display_name, v_description, v_ordering, v_is_active, v_is_default, v_is_checked, v_is_private, v_is_final );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_LOOKUPBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_LUPABYSIDFNAME" 
(
  v_sysId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_sysId
                 AND field_id = ( SELECT field_id
                                  FROM fields
                                     WHERE sys_id = v_sysId
                                             AND NAME = v_fieldName )
        ORDER BY ordering;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_LUPBYSIDFIDTYPEID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_LUPBYSIDFNAME" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_fieldId NUMBER(10,0);
BEGIN
   SELECT NVL(field_id, 0)
     INTO v_fieldId
     FROM fields
      WHERE sys_id = v_systemId
              AND NAME = v_fieldName;

   OPEN cv_1 FOR
      SELECT *
        FROM types
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPE_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_description IN NVARCHAR2 DEFAULT NULL ,
  v_ordering IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_is_default IN NUMBER DEFAULT NULL ,
  v_is_checked IN NUMBER DEFAULT NULL ,
  v_is_private IN NUMBER DEFAULT NULL ,
  v_is_final IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE types
      SET NAME = v_name,
          display_name = v_display_name,
          DESCRIPTION = v_description,
          is_default = v_is_default,
          is_active = v_is_active,
          is_checked = v_is_checked,
          is_private = v_is_private,
          is_final = v_is_final,
          ordering = v_ordering
      WHERE sys_id = v_sys_id
     AND field_id = v_field_id
     AND TYPE_ID = v_type_id;

   IF v_is_default = 1 THEN
   BEGIN
      UPDATE types
         SET is_default = 0
         WHERE sys_id = v_sys_id
        AND field_id = v_field_id
        AND TYPE_ID != v_type_id;

   END;
   ELSE
   DECLARE
      v_totalCount NUMBER(10,0);
      v_temp NUMBER(1, 0) := 0;
   BEGIN
      SELECT ( SELECT COUNT(*)
               FROM types
                  WHERE sys_id = v_sys_id
                          AND field_id = v_field_id )
        INTO v_totalCount
        FROM DUAL ;

      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          WHERE v_totalCount = ( SELECT COUNT(*)
                                 FROM types
                                    WHERE sys_id = v_sys_id
                                            AND field_id = v_field_id
                                            AND is_default = 0 );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp = 1 THEN
      BEGIN
         UPDATE types
            SET is_default = 1
            WHERE sys_id = v_sys_id
           AND field_id = v_field_id
           AND TYPE_ID = ( SELECT MIN(types.TYPE_ID)
                           FROM types
                              WHERE types.sys_id = v_sys_id
                                      AND types.field_id = v_field_id );

      END;
      END IF;

   END;
   END IF;

   NULL/*TODO:SET QUOTED_IDENTIFIER OFF*/;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPEUSER_DELETE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_notification_id IN NUMBER DEFAULT NULL ,
  v_is_volunteer IN NUMBER DEFAULT NULL ,
  v_rr_volunteer IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE type_users

      WHERE sys_id = v_sys_id
              AND field_id = v_field_id
              AND TYPE_ID = v_type_id
              AND USER_ID = v_user_id;

   NULL/*TODO:SET QUOTED_IDENTIFIER OFF*/;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPEUSER_GETALLTYPEUSERS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT tu.*
        FROM type_users tu
               JOIN users u
                ON tu.USER_ID = u.USER_ID
         WHERE tu.is_active = 1
                 AND u.is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPEUSER_INSERT" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_notification_id IN NUMBER DEFAULT NULL ,
  v_is_volunteer IN NUMBER DEFAULT NULL ,
  v_rr_volunteer IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   INSERT INTO type_users
     ( sys_id, field_id, TYPE_ID, USER_ID, user_type_id, notification_id, is_volunteer, rr_volunteer, is_active )
     VALUES ( v_sys_id, v_field_id, v_type_id, v_user_id, v_user_type_id, v_notification_id, v_is_volunteer, v_rr_volunteer, v_is_active );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPEUSER_LOOKUPBYSYSTEMID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM type_users
         WHERE sys_id = v_systemId
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYPEUSER_UPDATE" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_field_id IN NUMBER DEFAULT NULL ,
  v_type_id IN NUMBER DEFAULT NULL ,
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_notification_id IN NUMBER DEFAULT NULL ,
  v_is_volunteer IN NUMBER DEFAULT NULL ,
  v_rr_volunteer IN NUMBER DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE type_users
      SET notification_id = v_notification_id,
          is_volunteer = v_is_volunteer,
          rr_volunteer = v_rr_volunteer,
          is_active = v_is_active,
          user_type_id = v_user_type_id
      WHERE sys_id = v_sys_id
     AND field_id = v_field_id
     AND TYPE_ID = v_type_id
     AND USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYUSER_LUPSIDFIDTYID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM type_users
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYUSER_LUPSIDFIDTYIDUSID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM type_users
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId
                 AND USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_TYUSER_LUPVOSDFDTYPEID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_fieldId IN NUMBER DEFAULT NULL ,
  v_typeId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM type_users
         WHERE sys_id = v_systemId
                 AND field_id = v_fieldId
                 AND TYPE_ID = v_typeId
                 AND is_volunteer = 1
                 AND is_active = 1;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_UPDATE_LAST_ESC_TIME" 
(
  v_sys_id IN NUMBER DEFAULT NULL ,
  v_request_id IN NUMBER DEFAULT NULL ,
  v_val IN DATE DEFAULT NULL
)
AS
   v_temp NUMBER(1, 0) := 0;
BEGIN
   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE EXISTS ( SELECT *
                      FROM escalation_history
                         WHERE sys_id = v_sys_id
                                 AND request_id = v_request_id );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   -- SET NOCOUNT ON added to prevent extra result sets from
   -- interfering with SELECT statements.
   IF v_temp = 1 THEN
      UPDATE escalation_history
         SET last_escalated_time = v_val
         WHERE sys_id = v_sys_id
        AND request_id = v_request_id;

   ELSE
      INSERT INTO escalation_history
        ( sys_id, request_id, last_escalated_time )
        VALUES ( v_sys_id, v_request_id, v_val );

   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_URA_LUPSIDRTIDUSERID" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM user_read_actions
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_URA_REGISTERUSERREADACTION" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_actionId IN NUMBER DEFAULT NULL ,
  v_userId IN NUMBER DEFAULT NULL
)
AS
   /*
* If a Record with larger or equal actionId is present, simply return
*/
   v_prevActionId NUMBER(10,0);
   v_temp NUMBER(1, 0) := 0;
BEGIN
   v_prevActionId := 0;

   SELECT action_id
     INTO v_prevActionId
     FROM user_read_actions
      WHERE sys_id = v_systemId
              AND request_id = v_requestId
              AND USER_ID = v_userId;

   IF ( v_prevActionId >= v_actionId ) THEN
      RETURN;

   END IF;

   BEGIN
      SELECT 1 INTO v_temp
        FROM DUAL
       WHERE EXISTS ( SELECT *
                      FROM user_read_actions
                         WHERE sys_id = v_systemId
                                 AND request_id = v_requestId
                                 AND USER_ID = v_userId );
   EXCEPTION
      WHEN OTHERS THEN
         NULL;
   END;

   /*
 * Check if a record already exists.
 */
   IF v_temp = 1 THEN
   BEGIN
      /*
      * Update the record if it already exists.
      */
      UPDATE user_read_actions
         SET action_id = v_actionId
         WHERE sys_id = v_systemId
        AND request_id = v_requestId
        AND USER_ID = v_userId;

   END;
   ELSE
   BEGIN
      /*
      * Insert the record if it does not exist.
      */
      INSERT INTO user_read_actions
        ( sys_id, request_id, action_id, USER_ID )
        VALUES ( v_systemId, v_requestId, v_actionId, v_userId );

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_URA_REMOVEREQUESTENTRY" 
(
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE user_read_actions

      WHERE sys_id = v_systemId
              AND request_id = v_requestId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DR_LUBYUIDSIDDRAFTID" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_systemId IN NUMBER DEFAULT NULL ,
  v_draftId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM user_drafts
         WHERE sys_id = v_systemId
                 AND draft_id = v_draftId
                 AND USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DR_LUBYUIDSIDREQID" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM user_drafts
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DR_LUBYUIDSIDREQIDTS" /*stp_user_draft_lookupByUserIdAndSystemIdAndRequestIdAndTimestamp*/
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_systemId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_timeStamp IN DATE DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM user_drafts
         WHERE sys_id = v_systemId
                 AND request_id = v_requestId
                 AND USER_ID = v_userId
                 AND to_date(time_stamp) = to_date(v_timeStamp)
                 AND ABS( time_stamp- v_timeStamp) < 10;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DRAFT_DELETE" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_sysId IN NUMBER DEFAULT NULL ,
  v_draftId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   DELETE user_drafts

      WHERE USER_ID = v_userId
              AND sys_id = v_sysId
              AND draft_id = v_draftId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DRAFT_INSERT" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_timeStamp IN DATE DEFAULT NULL ,
  v_sysId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_draft IN CLOB DEFAULT NULL ,
  v_draftid OUT NUMBER
)
AS
   v_maxDraftId NUMBER(10,0);
BEGIN
   SELECT NVL(MAX(draft_id), 0)
     INTO v_maxDraftId
     FROM user_drafts ;

   v_draftId := v_maxDraftId + 1;

   --delete from  user_drafts
   --where
   --     user_id = @userId And
   --     sys_id=@sysId And
   --     request_id = @requestId And
   --     convert(smallDateTime, time_stamp) = convert(smallDateTime, @timeStamp) AND
   --     abs(Datepart(millisecond,time_stamp) - Datepart(millisecond,@timeStamp)) < 10
   INSERT INTO user_drafts
     ( USER_ID, time_stamp, sys_id, request_id, draft, draft_id )
     VALUES ( v_userId, v_timeStamp, v_sysId, v_requestId, v_draft, v_draftId );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DRAFT_LOOKUPBYUSERID" 
(
  v_userid IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM user_drafts ud
         WHERE USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_DRAFT_UPDATE" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  v_timeStamp IN DATE DEFAULT NULL ,
  v_sysId IN NUMBER DEFAULT NULL ,
  v_requestId IN NUMBER DEFAULT NULL ,
  v_draft IN CLOB DEFAULT NULL ,
  v_draftId IN NUMBER DEFAULT NULL
)
AS
BEGIN
   UPDATE user_drafts
      SET draft = v_draft
      WHERE USER_ID = v_userId
     AND sys_id = v_sysId
     AND draft_id = v_draftId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_EXPMAILLBYEMAIL" 
(
  iv_mailListName IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
   v_mailListName VARCHAR2(256) := iv_mailListName;
   v_temp NUMBER(1, 0) := 0;
BEGIN
   /*
 * If there is a * at the start, remove it and prepend % for like search.
 */
   IF ( INSTR(v_mailListName, '*') = 1 ) THEN
   BEGIN
      v_mailListName := '%' || SUBSTR(v_mailListName, 2, LENGTH(v_mailListName));

   END;
   END IF;

   /*
 * If there is a * at the end, remove it
 */
   IF ( INSTR(v_mailListName, '*') = LENGTH(v_mailListName) ) THEN
   BEGIN
      v_mailListName := SUBSTR(v_mailListName, 1, LENGTH(v_mailListName) - 1);

   END;
   END IF;

   -- Append % for like search.
   v_mailListName := v_mailListName || '%';

   /**
 * #tmp1 contains the internal_users
 * #tmp2 contains the internal_mailing_lists
 */
   INSERT INTO tt_tmp1
     ( SELECT ml.USER_ID,
              u.user_login,
              u.user_type_id
       FROM mail_list_users ml
              JOIN users uc
               ON ml.mail_list_id = uc.USER_ID
              JOIN users u
               ON ml.USER_ID = u.USER_ID
          WHERE uc.email LIKE v_mailListName
                  AND u.user_type_id <> 8 );

   INSERT INTO tt_tmp2
     ( SELECT ml.USER_ID,
              u.user_login,
              u.user_type_id
       FROM mail_list_users ml
              JOIN users u
               ON ml.USER_ID = u.USER_ID
              JOIN users uc
               ON ml.mail_list_id = uc.USER_ID
          WHERE uc.email LIKE v_mailListName
                  AND u.user_type_id = 8 );

   LOOP
      BEGIN
         SELECT 1 INTO v_temp
           FROM DUAL
          /**
 * As long as there are mailing lists in #tmp2, continue with resolving process.
 */
          WHERE ( EXISTS ( SELECT *
                                FROM tt_tmp2  ) );
      EXCEPTION
         WHEN OTHERS THEN
            NULL;
      END;

      IF v_temp != 1 THEN
         EXIT;
      END IF;

      BEGIN
         DELETE FROM tt_tmp3;

         INSERT INTO tt_tmp3 (
            /**
         * Get the list of users present in the mailing lists in #tmp2 who are not present in #tmp1
      */
            SELECT ml.USER_ID,
                        u.user_login,
                        u.user_type_id
              FROM mail_list_users ml
                     LEFT JOIN users u
                      ON ml.USER_ID = u.USER_ID
               WHERE ml.mail_list_id IN ( DELETE FROM tt_tmp3;

                                          INSERT INTO tt_tmp3 (
                                          SELECT USER_ID
                                          FROM tt_tmp2
                                             WHERE user_type_id = 8 ) )
                       AND u.USER_ID NOT IN ( DELETE FROM tt_tmp3;

                                              INSERT INTO tt_tmp3 (
                                              SELECT USER_ID
                                              FROM tt_tmp1
                                                 WHERE user_type_id = 7 ) ) );

         /**
      * Insert all the users into #tmp1.
      * Insert all the mailinglists at this level into #tmp2 after clearing the table.
      */
         INSERT INTO tt_tmp1
           ( SELECT *
             FROM tt_tmp3
                WHERE user_type_id <> 8 );

         -- Clear the earlier mailing lists
         DELETE tt_tmp2
         ;

         -- Get the fresh list of mailing lists found at this level.
         INSERT INTO tt_tmp2
           ( SELECT *
             FROM tt_tmp3
                WHERE user_type_id = 8 );

         -- Delete #tmp3.
         EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp3 ';

      END;
   END LOOP;

   OPEN cv_1 FOR
      /**
 * #tmp1 contains the final list of users for the given mailing list.
 */
      SELECT DISTINCT USER_ID
        FROM tt_tmp1 ;

   -- Drop the temporary tables.
   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp2 ';

   EXECUTE IMMEDIATE ' TRUNCATE TABLE tt_tmp1 ';

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_GETALLUSERS" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT USER_ID,
             user_login,
             first_name,
             last_name,
             display_name,
             email,
             is_active,
             user_type_id,
             web_config,
             windows_config,
             is_on_vacation,
             is_display,
             cn,
             distinguished_name,
             NAME,
             member_of,
             member,
             mail_nickname,
             location,
             extension,
             mobile,
             home_phone
        FROM users ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_INSERTEXTERNALUSER" 
(
  v_user_id OUT NUMBER,
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_first_name IN NVARCHAR2 DEFAULT NULL ,
  v_last_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_web_config IN CLOB DEFAULT NULL ,
  v_windows_config IN CLOB DEFAULT NULL ,
  v_is_on_vacation IN NUMBER DEFAULT NULL ,
  v_is_display IN NUMBER DEFAULT NULL ,
  v_cn IN VARCHAR2 DEFAULT NULL ,
  v_distinguished_name IN VARCHAR2 DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_member_of IN CLOB DEFAULT NULL ,
  v_member IN CLOB DEFAULT NULL ,
  v_mail_nickname IN VARCHAR2 DEFAULT NULL ,
  v_location IN VARCHAR2 DEFAULT NULL ,
  v_extension IN VARCHAR2 DEFAULT NULL ,
  v_mobile IN VARCHAR2 DEFAULT NULL ,
  v_home_phone IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN
   SELECT NVL(MAX(USER_ID), 50000)
     INTO v_user_id
     FROM users
      WHERE USER_ID >= 50000;

   v_user_id := v_user_id + 1;

   INSERT INTO users
     ( USER_ID, user_login, first_name, last_name, display_name, email, is_active, user_type_id, web_config, windows_config, is_on_vacation, is_display, cn, distinguished_name, NAME, member_of, member, mail_nickname, location, extension, mobile, home_phone )
     VALUES ( v_user_id, v_user_login, v_first_name, v_last_name, v_display_name, v_email, v_is_active, v_user_type_id, v_web_config, v_windows_config, v_is_on_vacation, v_is_display, v_cn, v_distinguished_name, v_name, v_member_of, v_member, v_mail_nickname, v_location, v_extension, v_mobile, v_home_phone );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_INSERTMINUSER" 
(
  /*
 * Inserts a user with min required values
 */
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL
)
AS
   v_user_id NUMBER(10,0);
BEGIN
   SELECT NVL(MAX(USER_ID), 0) + 1
     INTO v_user_id
     FROM users ;

   INSERT INTO users
     ( USER_ID, user_login, email, is_active )
     VALUES ( v_user_id, v_user_login, v_email, 1 );

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_LOOKUPBYEMAIL" 
(
  v_email IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT USER_ID,
             user_login,
             first_name,
             last_name,
             display_name,
             email,
             is_active,
             user_type_id,
             web_config,
             windows_config,
             is_on_vacation,
             is_display,
             cn,
             distinguished_name,
             NAME,
             member_of,
             member,
             mail_nickname,
             location,
             extension,
             mobile,
             home_phone
        FROM users
         WHERE email = v_email;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_LOOKUPBYLOGINPASSWORD" 
(
  v_userLogin IN VARCHAR2 DEFAULT NULL ,
  v_password IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT user_login
        FROM user_passwords
         WHERE user_login = v_userLogin
                 AND PASSWORD = v_password;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_LOOKUPBYUSERID" 
(
  v_userId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT USER_ID,
             user_login,
             first_name,
             last_name,
             display_name,
             email,
             is_active,
             user_type_id,
             web_config,
             windows_config,
             is_on_vacation,
             is_display,
             cn,
             distinguished_name,
             NAME,
             member_of,
             member,
             mail_nickname,
             location,
             extension,
             mobile,
             home_phone
        FROM users
         WHERE USER_ID = v_userId;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_LOOKUPBYUSERLOGIN" 
(
  v_userLogin IN VARCHAR2 DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT USER_ID,
             user_login,
             first_name,
             last_name,
             display_name,
             email,
             is_active,
             user_type_id,
             web_config,
             windows_config,
             is_on_vacation,
             is_display,
             cn,
             distinguished_name,
             NAME,
             member_of,
             member,
             mail_nickname,
             location,
             extension,
             mobile,
             home_phone
        FROM users
         WHERE user_login = v_userLogin;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_LOOKUPBYUSERLOGINLIKE" 
(
  v_userLogin IN VARCHAR2 DEFAULT NULL ,
  v_noInActive IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   IF ( v_noInActive = 0 ) THEN
   BEGIN
      OPEN cv_1 FOR
         SELECT USER_ID
           FROM users
            WHERE user_login LIKE v_userLogin || '%';

   END;
   ELSE
   BEGIN
      OPEN cv_1 FOR
         SELECT USER_ID
           FROM users
            WHERE user_login LIKE v_userLogin || '%'
                    AND is_active = 1;

   END;
   END IF;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_USER_UPDATE" 
(
  v_user_id IN NUMBER DEFAULT NULL ,
  v_user_login IN NVARCHAR2 DEFAULT NULL ,
  v_first_name IN NVARCHAR2 DEFAULT NULL ,
  v_last_name IN NVARCHAR2 DEFAULT NULL ,
  v_display_name IN NVARCHAR2 DEFAULT NULL ,
  v_email IN NVARCHAR2 DEFAULT NULL ,
  v_is_active IN NUMBER DEFAULT NULL ,
  v_user_type_id IN NUMBER DEFAULT NULL ,
  v_web_config IN CLOB DEFAULT NULL ,
  v_windows_config IN CLOB DEFAULT NULL ,
  v_is_on_vacation IN NUMBER DEFAULT NULL ,
  v_is_display IN NUMBER DEFAULT NULL ,
  v_cn IN VARCHAR2 DEFAULT NULL ,
  v_distinguished_name IN VARCHAR2 DEFAULT NULL ,
  v_name IN VARCHAR2 DEFAULT NULL ,
  v_member_of IN CLOB DEFAULT NULL ,
  v_member IN CLOB DEFAULT NULL ,
  v_mail_nickname IN VARCHAR2 DEFAULT NULL ,
  v_location IN VARCHAR2 DEFAULT NULL ,
  v_extension IN VARCHAR2 DEFAULT NULL ,
  v_mobile IN VARCHAR2 DEFAULT NULL ,
  v_home_phone IN VARCHAR2 DEFAULT NULL
)
AS
BEGIN
   UPDATE users
      SET user_login = v_user_login,
          first_name = v_first_name,
          last_name = v_last_name,
          display_name = v_display_name,
          email = v_email,
          is_active = v_is_active,
          user_type_id = v_user_type_id,
          web_config = v_web_config,
          windows_config = v_windows_config,
          is_on_vacation = v_is_on_vacation,
          is_display = v_is_display,
          cn = v_cn,
          distinguished_name = v_distinguished_name,
          NAME = v_name,
          member_of = v_member_of,
          member = v_member,
          mail_nickname = v_mail_nickname,
          location = v_location,
          extension = v_extension,
          mobile = v_mobile,
          home_phone = v_home_phone
      WHERE USER_ID = v_user_id;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_WR_GETALLWORKFLOWRULES" 
(
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM workflow_rules ;

END;
/
 
  CREATE OR REPLACE PROCEDURE "VIK"."STP_WR_LOOKUPBYRULEID" 
(
  v_ruleId IN NUMBER DEFAULT NULL ,
  cv_1 IN OUT SYS_REFCURSOR
)
AS
BEGIN
   OPEN cv_1 FOR
      SELECT *
        FROM workflow_rules
         WHERE rule_id = v_ruleId;

END;
/
 