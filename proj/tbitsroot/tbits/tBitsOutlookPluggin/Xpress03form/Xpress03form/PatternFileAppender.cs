using System;
using System.IO;
using System.Web.Mail;

using log4net.Layout;
using log4net.Core;
using log4net.Appender;
using System.Text;
using log4net.Util;
namespace Xpress03form
{
        public class PatternFileAppender : AppenderSkeleton
        {
            public PatternFileAppender()
            {
            }

            public PatternLayout File
            {
                get { return m_filePattern; }
                set { m_filePattern = value; }
            }

            public Encoding Encoding
            {
                get { return m_encoding; }
                set { m_encoding = value; }
            }

            public SecurityContext SecurityContext
            {
                get { return m_securityContext; }
                set { m_securityContext = value; }
            }

            override public void ActivateOptions()
            {
                base.ActivateOptions();

                if (m_securityContext == null)
                {
                    m_securityContext = SecurityContextProvider.DefaultProvider.CreateSecurityContext(this);
                }
            }

            override protected void Append(LoggingEvent loggingEvent)
            {
                try
                {
                    // Render the file name
                    StringWriter stringWriter = new StringWriter();
                    m_filePattern.Format(stringWriter, loggingEvent);
                    string fileName = stringWriter.ToString();

                    fileName = SystemInfo.ConvertToFullPath(fileName);

                    FileStream fileStream = null;

                    using (m_securityContext.Impersonate(this))
                    {
                        // Ensure that the directory structure exists
                        string directoryFullName = Path.GetDirectoryName(fileName);

                        // Only create the directory if it does not exist
                        // doing this check here resolves some permissions failures
                        if (!Directory.Exists(directoryFullName))
                        {
                            Directory.CreateDirectory(directoryFullName);
                        }

                        // Open file stream while impersonating
                        fileStream = new FileStream(fileName, FileMode.Append, FileAccess.Write, FileShare.Read);
                    }

                    if (fileStream != null)
                    {
                        using (StreamWriter streamWriter = new StreamWriter(fileStream, m_encoding))
                        {
                            RenderLoggingEvent(streamWriter, loggingEvent);
                        }

                        fileStream.Close();
                    }
                }
                catch (Exception ex)
                {
                    ErrorHandler.Error("Failed to append to file", ex);
                }
            }

            private PatternLayout m_filePattern = null;
            private Encoding m_encoding = Encoding.Default;
            private SecurityContext m_securityContext;
        }
    }

