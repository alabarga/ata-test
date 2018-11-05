using System;
using System.Collections.Generic;
using System.Text;

namespace Integromics.RClient
{
    /// <summary>
    /// temp files support;
    /// </summary>
    internal class TempFilesPool
    {
        
        /// <summary>
        /// ctor
        /// </summary>
        /// <param name="strRelativePath">Relative path in 'ApplicationData' folder</param> 
        public TempFilesPool(string strRelativePath)
        {
            try
            {
                StringBuilder strbTempFolder = new StringBuilder();
                strbTempFolder.Append(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData));
                strbTempFolder.Append(strRelativePath);

                System.IO.Directory.CreateDirectory(strbTempFolder.ToString());

                m_strTempFolder = strbTempFolder.ToString();
            }
            catch (Exception exception)
            {
                
                throw exception;
            }
        }

        /// <summary>
        /// dtor
        /// </summary>
        ~TempFilesPool()
        {
            try
            {
                foreach (string strTempFile in m_alTempFiles)
                {
                    if (System.IO.File.Exists(strTempFile))
                        System.IO.File.Delete(strTempFile);
                }

                m_alTempFiles.Clear();
                m_alTempFiles = null;
            }
            catch (Exception exception)
            {
                throw exception;
            }
        }

        /// <summary>
        /// generates unique name for temp file
        /// </summary>
        /// <returns></returns>
        public string GenerateTempFileUniqueName()
        {
            StringBuilder strbTempFile = new StringBuilder();
            strbTempFile.Append(m_strTempFolder);
            strbTempFile.AppendFormat("\\_temp_{0}.txt", DateTime.Now.Ticks);

            m_alTempFiles.Add(strbTempFile.ToString());

            return strbTempFile.ToString();
        }

        /// <summary>
        /// full path to the temp folder
        /// </summary>
        protected string m_strTempFolder = null;

        /// <summary>
        /// list of temp files
        /// </summary>
        protected System.Collections.ArrayList m_alTempFiles = new System.Collections.ArrayList();
    }
}
