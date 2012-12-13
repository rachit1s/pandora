IF NOT EXISTS (SELECT * FROM dbo.tbits_properties WHERE name='transbit.tbits.mail.sendWelcomeMail')
BEGIN
INSERT INTO dbo.tbits_properties
        ( name ,
          value ,
          displayName ,
          description ,
          category ,
          type
        )
VALUES  ( 'transbit.tbits.mail.sendWelcomeMail' , -- name - varchar(150)
          'false' , -- value - text
          'Send Welcome Mail to Auto Added User from Email' , -- displayName - varchar(100)
          'Whether a welcome mail with login/password to the new automatically created user be sent or not.' , -- description - text
          'email' , -- category - varchar(150)
          'boolean'  -- type - varchar(50)
        )
END
