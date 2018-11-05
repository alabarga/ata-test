using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.Serialization;

using Spotfire.Dxp.Application;
using Spotfire.Dxp.Data;
using Spotfire.Dxp.Application.Extension;
using Spotfire.Dxp.Application.Layout;
using Spotfire.Dxp.Framework.ApplicationModel;
using Spotfire.Dxp.Framework.DocumentModel;
using Spotfire.Dxp.Framework.License;
using Spotfire.Dxp.Framework.Persistence;

using Integromics.StatConnectorSrv;
using Integromics.StatConnectorClnt;

namespace Integromics.RClient
{
    /// <summary>
    /// C# R-Client
    /// </summary>
    public class CRClient 
        : Integromics.StatConnectorClnt.StatConnectorCharacterDevice         
        , IDisposable
    {
        #region Members

        /// <summary>
        /// points the AnalysisApplication (Spotfire DXP client)
        /// </summary>
        protected AnalysisApplication m_analysisApplication = null;

        /// <summary>
        /// R DCOM Server
        /// </summary>
        protected StatConnector m_rServer = null;

        /// <summary>
        /// error description
        /// </summary>
        protected string m_strError = null;

        /// <summary>
        /// temp files pool
        /// </summary>
        TempFilesPool m_tempFilesPool = null;

        #endregion // Members

        #region Methods

        #region Construction/Destruction

        /// <summary>
        /// ctor
        /// </summary>
        public CRClient(AnalysisApplication analysisApplication)
        {
            try
            {
                if (analysisApplication == null)
                    throw new ArgumentNullException("analysisApplication");

                m_analysisApplication = analysisApplication;

                Init();

                m_tempFilesPool = new TempFilesPool("\\Integromics\\Spotfire DXP\\R-Client\\Temp");
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::CRClient().", exception);
            }
        }

        /// <summary>
        /// dtor
        /// </summary>
        ~CRClient()
        {
            this.Dispose();
        }

        #endregion // Construction/Destruction

        /// <summary>
        /// init functionality
        /// </summary>
        /// <returns></returns>
        protected bool Init()
        {
            try
            {
                if (m_rServer == null)
                {
                    m_rServer = new StatConnector();
                    m_rServer.Init("R");
                    m_rServer.SetCharacterOutputDevice(this);
                }                

                return true;
            }            
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::Init().", exception);
            }
        }

        /// <summary>
        /// reset functionality
        /// </summary>
        /// <returns></returns>
        public bool Reset()
        {
            try
            {
                // reset - reset all vars
                ExecuteRScriptNoResult("rm(list=ls(all=TRUE))");

                return true;
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::Init().", exception);
            }
        }

        /// <summary>
        /// executes R script - doesn't return result
        /// </summary>
        /// <param name="strRScript"></param>
        /// <returns></returns>
        public bool ExecuteRScriptNoResult(string strRScript)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");

                try
                {
                    m_rServer.EvaluateNoReturn(strRScript);
                }
                catch
                {
                    throw new Exception(this.Error);
                } 

                return true;
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScriptNoResult()", exception);
            }            
        }

        /// <summary>
        /// executes R script - returns result
        /// </summary>
        /// <param name="strRScript"></param>
        /// <returns></returns>
        public Object ExecuteRScript(string strRScript)
        {
            try
            {
                m_strError = null;               

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");

                Object result = null;

                try
                {
                    result = m_rServer.Evaluate(strRScript);
                }
                catch
                {
                    throw new Exception(this.Error);
                } 

                return result;
            }            
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScriptNoResult()", exception);
            } 
        }

        /// <summary>
        /// executes R script and exports desired R-variable to Spotfire DXP
        /// </summary>
        /// <param name="strRScript"></param>
        /// <param name="strRVariable"></param>
        /// <param name="strTableName"></param>
        /// <param name="bOverrideExisting"></param>
        public void ExecuteRScriptExportResult(string strRScript, string strRVariable, string strTableName, bool bOverrideExisting)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");                        

                // extract all R-scripts
                string[] arRScripts = strRScript.Split(new char[] { '\n' }, StringSplitOptions.RemoveEmptyEntries);

                // execute R-scripts one by one
                for (int iRScript = 0; iRScript < arRScripts.Length; iRScript++)
                {
                    arRScripts[iRScript] = arRScripts[iRScript].Trim();
                    if (arRScripts[iRScript].Length == 0)
                        continue;

                    try
                    {
                        m_rServer.EvaluateNoReturn(arRScripts[iRScript]);
                    }
                    catch
                    {
                        throw new Exception(this.Error);
                    }  
                }

                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                // export results
                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append("write.table( ");
                strbRScript.Append(strRVariable);
                strbRScript.Append(", \"");
                strbRScript.Append(strTempFile);
                strbRScript.Append("\", quote=FALSE, sep=\"\t\", col.names=NA )");
                strbRScript.Replace("\\", "\\\\");

                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {
                    throw new Exception(this.Error);
                }    

                DataSource dataSource = m_analysisApplication.Document.Data.CreateFileDataSource(strTempFile);

                if (bOverrideExisting && m_analysisApplication.Document.Data.Tables.Contains(strTableName))
                {
                    DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];
                    dataTable.ReplaceData(dataSource);
                }
                else
                {
                    string strNewTableName = m_analysisApplication.Document.Data.Tables.CreateUniqueName(strTableName);

                    m_analysisApplication.Document.Data.Tables.Add(strNewTableName, dataSource);
                }
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScriptExportResult().", exception);
            }
        }

        /// <summary>
        /// executes R script and exports desired R-variable to Spotfire DXP
        /// </summary>
        /// <param name="strRScript"></param>
        /// <param name="strRVariable"></param>
        /// <param name="strTableName"></param>
        /// <param name="bOverrideExisting"></param>
        public void ExecuteRScriptExportResult_ExecuteWithProgress(string strRScript, string strRVariable, string strTableName, bool bOverrideExisting)
        {
            try
            {
                Spotfire.Dxp.Framework.ApplicationModel.ProgressService progress = m_analysisApplication.GetService<Spotfire.Dxp.Framework.ApplicationModel.ProgressService>();

                progress.ExecuteWithProgress("R-Client", "Executing R scripts...",
                    delegate
                    {
                        m_strError = null;

                        if (m_rServer == null)
                            throw new Exception("R-Client is not initialized.");

                        // extract all R-scripts
                        string[] arRScripts = strRScript.Split(new char[] { '\n' }, StringSplitOptions.RemoveEmptyEntries);

                        // execute R-scripts one by one
                        for (int iRScript = 0; iRScript < arRScripts.Length; iRScript++)
                        {
                            arRScripts[iRScript] = arRScripts[iRScript].Trim();
                            if (arRScripts[iRScript].Length == 0)
                                continue;

                            try
                            {
                                m_rServer.EvaluateNoReturn(arRScripts[iRScript]);
                            }
                            catch
                            {
                                throw new Exception(this.Error);
                            }
                        }

                        string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                        // export results
                        StringBuilder strbRScript = new StringBuilder();
                        strbRScript.Append("write.table( ");
                        strbRScript.Append(strRVariable);
                        strbRScript.Append(", \"");
                        strbRScript.Append(strTempFile);
                        strbRScript.Append("\", quote=FALSE, sep=\"\t\", col.names=NA )");
                        strbRScript.Replace("\\", "\\\\");

                        try
                        {
                            m_rServer.EvaluateNoReturn(strbRScript.ToString());
                        }
                        catch
                        {
                            throw new Exception(this.Error);
                        }

                        DataSource dataSource = m_analysisApplication.Document.Data.CreateFileDataSource(strTempFile);

                        if (bOverrideExisting && m_analysisApplication.Document.Data.Tables.Contains(strTableName))
                        {
                            DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];
                            dataTable.ReplaceData(dataSource);
                        }
                        else
                        {
                            string strNewTableName = m_analysisApplication.Document.Data.Tables.CreateUniqueName(strTableName);

                            m_analysisApplication.Document.Data.Tables.Add(strNewTableName, dataSource);
                        }
                    }
                );
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScriptExportResult_ExecuteWithProgress().", exception);
            }
        }

        /// <summary>
        /// executes R scripts
        /// </summary>
        /// <param name="strRScript"></param>
        public void ExecuteRScripts(string strRScript)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");

                // extract all R-scripts
                string[] arRScripts = strRScript.Split(new char[] { '\n' }, StringSplitOptions.RemoveEmptyEntries);

                // execute R-scripts one by one
                for (int iRScript = 0; iRScript < arRScripts.Length; iRScript++)
                {
                    arRScripts[iRScript] = arRScripts[iRScript].Trim();
                    if (arRScripts[iRScript].Length == 0)
                        continue;

                    try
                    {
                        m_rServer.EvaluateNoReturn(arRScripts[iRScript]);
                    }
                    catch
                    {
                        throw new Exception(this.Error);
                    }
                }
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScripts().", exception);
            }
        }

        /// <summary>
        /// executes R scripts
        /// </summary>
        /// <param name="strRScript"></param>
        /// <returns></returns>
        public void ExecuteRScripts_ExecuteWithProgress(string strRScript)
        {
            try
            {
                Spotfire.Dxp.Framework.ApplicationModel.ProgressService progress = m_analysisApplication.GetService<Spotfire.Dxp.Framework.ApplicationModel.ProgressService>();

                progress.ExecuteWithProgress("R-Client", "Executing R scripts...",
                    delegate
                    {
                        m_strError = null;

                        if (m_rServer == null)
                            throw new Exception("R-Client is not initialized.");

                        // extract all R-scripts
                        string[] arRScripts = strRScript.Split(new char[] { '\n' }, StringSplitOptions.RemoveEmptyEntries);

                        // execute R-scripts one by one
                        for (int iRScript = 0; iRScript < arRScripts.Length; iRScript++)
                        {
                            arRScripts[iRScript] = arRScripts[iRScript].Trim();
                            if (arRScripts[iRScript].Length == 0)
                                continue;

                            try
                            {
                                m_rServer.EvaluateNoReturn(arRScripts[iRScript]);
                            }
                            catch
                            {
                                throw new Exception(this.Error);
                            }
                        }
                    }
                );
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExecuteRScripts_ExecuteWithProgress().", exception);
            }
        }

        /// <summary>
        /// exports desired R-variable to Spotfire DXP
        /// </summary>
        /// <param name="strRVariable"></param>
        /// <param name="strTableName"></param>
        /// <param name="bOverrideExisting"></param>
        public void ExportTable(string strRVariable, string strTableName, bool bOverrideExisting)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");                       

                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                // export results
                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append("write.table( ");
                strbRScript.Append(strRVariable);
                strbRScript.Append(", \"");
                strbRScript.Append(strTempFile);
                strbRScript.Append("\", quote=FALSE, sep=\"\t\", col.names=NA )");
                strbRScript.Replace("\\", "\\\\");

                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {
                    throw new Exception(this.Error);
                }

                DataSource dataSource = m_analysisApplication.Document.Data.CreateFileDataSource(strTempFile);

                if (bOverrideExisting && m_analysisApplication.Document.Data.Tables.Contains(strTableName))
                {
                    DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];
                    dataTable.ReplaceData(dataSource);
                }
                else
                {
                    string strNewTableName = m_analysisApplication.Document.Data.Tables.CreateUniqueName(strTableName);

                    m_analysisApplication.Document.Data.Tables.Add(strNewTableName, dataSource);
                }
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExportTable().", exception);
            }
        }

        /// <summary>
        /// exports desired R-variable to Spotfire DXP
        /// </summary>
        /// <param name="strRVariable"></param>
        /// <param name="strTableName"></param>
        /// <param name="bOverrideExisting"></param>
        public void ExportTable_ExecuteWithProgress(string strRVariable, string strTableName, bool bOverrideExisting)
        {
            try
            {
                Spotfire.Dxp.Framework.ApplicationModel.ProgressService progress = m_analysisApplication.GetService<Spotfire.Dxp.Framework.ApplicationModel.ProgressService>();

                progress.ExecuteWithProgress("R-Client", "Executing R scripts...",
                    delegate
                    {
                        m_strError = null;

                        if (m_rServer == null)
                            throw new Exception("R-Client is not initialized.");

                        string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                        // export results
                        StringBuilder strbRScript = new StringBuilder();
                        strbRScript.Append("write.table( ");
                        strbRScript.Append(strRVariable);
                        strbRScript.Append(", \"");
                        strbRScript.Append(strTempFile);
                        strbRScript.Append("\", quote=FALSE, sep=\"\t\", col.names=NA )");
                        strbRScript.Replace("\\", "\\\\");

                        try
                        {
                            m_rServer.EvaluateNoReturn(strbRScript.ToString());
                        }
                        catch
                        {
                            throw new Exception(this.Error);
                        }

                        DataSource dataSource = m_analysisApplication.Document.Data.CreateFileDataSource(strTempFile);

                        if (bOverrideExisting && m_analysisApplication.Document.Data.Tables.Contains(strTableName))
                        {
                            DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];
                            dataTable.ReplaceData(dataSource);
                        }
                        else
                        {
                            string strNewTableName = m_analysisApplication.Document.Data.Tables.CreateUniqueName(strTableName);

                            m_analysisApplication.Document.Data.Tables.Add(strNewTableName, dataSource);
                        }
                    }
                );
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ExportTable_ExecuteWithProgress().", exception);
            }
        }

        /// <summary>
        /// imports desired table in R
        /// </summary>
        /// <param name="strTableName"></param>
        /// <param name="strRVariable"></param>
        public void ImportTable(string strTableName, string strRVariable)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");                        

                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                System.IO.StreamWriter file = System.IO.File.CreateText(strTempFile);

                Spotfire.Dxp.Data.DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];

                StringBuilder strbColumnNames = new StringBuilder();
                for (int iColumn = 0; iColumn < dataTable.Columns.Count; iColumn++)
                {
                    Spotfire.Dxp.Data.DataColumn dataColumn = dataTable.Columns[iColumn];
                    strbColumnNames.Append(dataColumn.Name);

                    if (iColumn < dataTable.Columns.Count - 1)
                        strbColumnNames.Append("\t");
                }

                strbColumnNames.Append("\n");

                file.WriteLine(strbColumnNames.ToString());

                for (int iRow = 0; iRow < dataTable.RowCount; iRow++)
                {
                    StringBuilder strbRow = new StringBuilder();
                    for (int iColumn = 0; iColumn < dataTable.Columns.Count; iColumn++)
                    {
                        if (!dataTable.Columns[iColumn].RowValues.IsInvalid(iRow))
                            strbRow.Append(dataTable.Columns[iColumn].RowValues.GetFormattedValue(iRow));
                        else
                            strbRow.Append("");

                        if (iColumn < dataTable.Columns.Count - 1)
                            strbRow.Append("\t");
                    }

                    strbRow.Append("\n");

                    file.WriteLine(strbRow.ToString());
                }

                file.Flush();
                file.Close();

                // export results
                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append(strRVariable);
		        strbRScript.Append("<-read.delim( \"");
                strbRScript.Append(strTempFile);
		        strbRScript.Append("\", quote=\"\", fill=TRUE, header=TRUE, na.strings=list(\"1.#INF\", \"\"), comment.char=\"\" )");
                strbRScript.Replace("\\", "\\\\");
                
                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {                            
                    throw new Exception(this.Error);
                }   
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ImportTable().", exception);
            }
        }

        /// <summary>
        /// imports desired table in R
        /// </summary>
        /// <param name="strTableName"></param>
        /// <param name="strRVariable"></param>
        public void ImportTable(string strTableName, string strRVariable, List<string> lstColumns)
        {
            try
            {
                m_strError = null;

                if (m_rServer == null)
                    throw new Exception("R-Client is not initialized.");

                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                System.IO.StreamWriter file = System.IO.File.CreateText(strTempFile);

                Spotfire.Dxp.Data.DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];

                StringBuilder strbColumnNames = new StringBuilder();

                List<string> lstIncludedColumns = new List<string>();
                if ((lstColumns == null) || (lstColumns.Count == 0))
                {
                    for (int iColumn = 0; iColumn < dataTable.Columns.Count; iColumn++)
                    {
                        Spotfire.Dxp.Data.DataColumn dataColumn = dataTable.Columns[iColumn];
                        strbColumnNames.Append(dataColumn.Name);

                        if (iColumn < dataTable.Columns.Count - 1)
                            strbColumnNames.Append("\t");

                        lstIncludedColumns.Add(dataColumn.Name);
                    }
                }
                else
                {
                    for (int iColumn = 0; iColumn < lstColumns.Count; iColumn++)
                    {
                        Spotfire.Dxp.Data.DataColumn dataColumn = dataTable.Columns[lstColumns[iColumn]];
                        strbColumnNames.Append(dataColumn.Name);

                        if (iColumn < lstColumns.Count - 1)
                            strbColumnNames.Append("\t");

                        lstIncludedColumns.Add(lstColumns[iColumn]);
                    }                    
                }

                strbColumnNames.Append("\n");

                file.WriteLine(strbColumnNames.ToString());

                for (int iRow = 0; iRow < dataTable.RowCount; iRow++)
                {
                    StringBuilder strbRow = new StringBuilder();
                    for (int iColumn = 0; iColumn < lstIncludedColumns.Count; iColumn++)
                    {
                        if (!dataTable.Columns[lstIncludedColumns[iColumn]].RowValues.IsInvalid(iRow))
                            strbRow.Append(dataTable.Columns[lstIncludedColumns[iColumn]].RowValues.GetFormattedValue(iRow));
                        else
                            strbRow.Append("");

                        if (iColumn < lstIncludedColumns.Count - 1)
                            strbRow.Append("\t");
                    }

                    strbRow.Append("\n");

                    file.WriteLine(strbRow.ToString());
                }

                file.Flush();
                file.Close();

                // export results
                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append(strRVariable);
                strbRScript.Append("<-read.delim( \"");
                strbRScript.Append(strTempFile);
                strbRScript.Append("\", quote=\"\", fill=TRUE, header=TRUE, na.strings=list(\"1.#INF\", \"\"), comment.char=\"\" )");
                strbRScript.Replace("\\", "\\\\");

                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {
                    throw new Exception(this.Error);
                }
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ImportTable().", exception);
            }
        }

        /// <summary>
        /// imports desired table in R
        /// </summary>
        /// <param name="strTableName"></param>
        /// <param name="strRVariable"></param>
        public void ImportTable_ExecuteWithProgress(string strTableName, string strRVariable, List<string> lstColumns)
        {
            try
            {
                Spotfire.Dxp.Framework.ApplicationModel.ProgressService progress = m_analysisApplication.GetService<Spotfire.Dxp.Framework.ApplicationModel.ProgressService>();

                progress.ExecuteWithProgress("R-Client", "Importing Table...",
                    delegate
                    {
                        m_strError = null;

                        if (m_rServer == null)
                            throw new Exception("R-Client is not initialized.");

                        string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                        System.IO.StreamWriter file = System.IO.File.CreateText(strTempFile);

                        Spotfire.Dxp.Data.DataTable dataTable = m_analysisApplication.Document.Data.Tables[strTableName];

                        StringBuilder strbColumnNames = new StringBuilder();

                        List<string> lstIncludedColumns = new List<string>();
                        if ((lstColumns == null) || (lstColumns.Count == 0))
                        {
                            for (int iColumn = 0; iColumn < dataTable.Columns.Count; iColumn++)
                            {
                                Spotfire.Dxp.Data.DataColumn dataColumn = dataTable.Columns[iColumn];
                                strbColumnNames.Append(dataColumn.Name);

                                if (iColumn < dataTable.Columns.Count - 1)
                                    strbColumnNames.Append("\t");

                                lstIncludedColumns.Add(dataColumn.Name);
                            }
                        }
                        else
                        {
                            for (int iColumn = 0; iColumn < lstColumns.Count; iColumn++)
                            {
                                Spotfire.Dxp.Data.DataColumn dataColumn = dataTable.Columns[lstColumns[iColumn]];
                                strbColumnNames.Append(dataColumn.Name);

                                if (iColumn < lstColumns.Count - 1)
                                    strbColumnNames.Append("\t");

                                lstIncludedColumns.Add(lstColumns[iColumn]);
                            }
                        }

                        strbColumnNames.Append("\n");

                        file.WriteLine(strbColumnNames.ToString());

                        for (int iRow = 0; iRow < dataTable.RowCount; iRow++)
                        {
                            StringBuilder strbRow = new StringBuilder();
                            for (int iColumn = 0; iColumn < lstIncludedColumns.Count; iColumn++)
                            {
                                if (!dataTable.Columns[lstIncludedColumns[iColumn]].RowValues.IsInvalid(iRow))
                                    strbRow.Append(dataTable.Columns[lstIncludedColumns[iColumn]].RowValues.GetFormattedValue(iRow));
                                else
                                    strbRow.Append("");

                                if (iColumn < lstIncludedColumns.Count - 1)
                                    strbRow.Append("\t");
                            }

                            strbRow.Append("\n");

                            file.WriteLine(strbRow.ToString());
                        }

                        file.Flush();
                        file.Close();

                        // export results
                        StringBuilder strbRScript = new StringBuilder();
                        strbRScript.Append(strRVariable);
                        strbRScript.Append("<-read.delim( \"");
                        strbRScript.Append(strTempFile);
                        strbRScript.Append("\", quote=\"\", fill=TRUE, header=TRUE, na.strings=list(\"1.#INF\", \"\"), comment.char=\"\" )");
                        strbRScript.Replace("\\", "\\\\");

                        try
                        {
                            m_rServer.EvaluateNoReturn(strbRScript.ToString());
                        }
                        catch
                        {
                            throw new Exception(this.Error);
                        }
                    }
                );
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::ImportTable_ExecuteWithProgress().", exception);
            }
        }        

        /// <summary>
        /// retrieves the Error
        /// </summary>
        protected string Error
        {
            get
            {
                if ((m_strError == null) || (m_strError.Length == 0))
                {
                    if (m_rServer != null)
                        m_strError = m_rServer.GetErrorText();

                    if ((m_strError != null) && (m_strError.Length > 0))
                        m_strError = "Error: " + m_strError + ".";
                }

                if ((m_strError == null) || (m_strError.Length == 0))
                    m_strError = "Unknown.";

                return m_strError;
            }
        }

        #region IStatConnectorCharacterDevice Members

        /// <summary>
        /// Implements IStatConnectorCharacterDevice.Clear()
        /// </summary>
        public void Clear()
        {
        }

        /// <summary>
        /// Implements IStatConnectorCharacterDevice.WriteString()
        /// </summary>
        /// <param name="bstrLine"></param>
        public void WriteString(string bstrLine)
        {
            m_strError = bstrLine;
        }

        /// <summary>
        /// Implements IStatConnectorCharacterDevice.WriteStringLevel()
        /// </summary>
        /// <param name="bstrLine"></param>
        /// <param name="lLevel"></param>
        public void WriteStringLevel(string bstrLine, int lLevel)
        {
        }

        #endregion // IStatConnectorCharacterDevice Members

        #region Serialization

        /// <summary>
        /// Serialization support
        /// </summary>
        /// <param name="info"></param>
        /// <param name="context"></param>
        protected void Save(SerializationInfo info, StreamingContext context)
        {
            try
            {
                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append("save.image(\"");
                strbRScript.Append(strTempFile);
                strbRScript.Append("\")");
                strbRScript.Replace("\\", "\\\\");

                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {
                    throw new Exception(this.Error);
                }

                EmbeddedResource embededResource = new EmbeddedResource();

                using (System.IO.FileStream rSession = System.IO.File.Open(strTempFile, System.IO.FileMode.Open))
                {
                    using (System.IO.Stream writeStream = embededResource.OpenWrite())
                    {
                        byte [] arBuffer = new byte [1024];
                        int iOffset = 0;
                        int iBytesRead = 0;

                        while ((iBytesRead = rSession.Read(arBuffer, iOffset, 1024)) > 0)
                        {
                            writeStream.Write(arBuffer, iOffset, iBytesRead);
                            iOffset += iBytesRead;

                            if ((long)iOffset >= rSession.Length)
                                break;
                        }
                        
                        writeStream.Close();
                    } // using (System.IO.Stream writeStream = ...

                    rSession.Close();
                } // using (System.IO.FileStream rSession = ...

                info.AddValue("Integromics.RClient.CRClientState", embededResource);
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::Save()", exception);
            }           
        }

        /// <summary>
        /// Serialization support
        /// </summary>
        /// <param name="info"></param>
        /// <param name="context"></param>
        protected void Load(SerializationInfo info, StreamingContext context)
        {
            try
            {
                string strTempFile = m_tempFilesPool.GenerateTempFileUniqueName();

                EmbeddedResource embededResource = (EmbeddedResource)info.GetValue("Integromics.RClient.CRClientState", typeof(EmbeddedResource));

                using (System.IO.FileStream rSession = System.IO.File.Open(strTempFile, System.IO.FileMode.CreateNew))
                {
                    using (System.IO.Stream readStream = embededResource.OpenRead())
                    {
                        byte[] arBuffer = new byte[1024];
                        int iOffset = 0;
                        int iBytesRead = 0;

                        while ((iBytesRead = readStream.Read(arBuffer, iOffset, 1024)) > 0)
                        {
                            rSession.Write(arBuffer, iOffset, iBytesRead);
                            iOffset += iBytesRead;

                            if ((long)iOffset >= readStream.Length)
                                break;
                        }

                        readStream.Close();
                    } // using (System.IO.Stream writeStream = ...

                    rSession.Close();
                } // using (System.IO.FileStream rSession = ...

                StringBuilder strbRScript = new StringBuilder();
                strbRScript.Append("load(\"");
                strbRScript.Append(strTempFile);
                strbRScript.Append("\")");
                strbRScript.Replace("\\", "\\\\");

                try
                {
                    m_rServer.EvaluateNoReturn(strbRScript.ToString());
                }
                catch
                {
                    throw new Exception(this.Error);
                }
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::Save()", exception);
            }
        }

        #endregion // Serialization

        #region IDisposable Members

        /// <summary>
        /// Implements IDisposable.Dispose()
        /// </summary>
        public void Dispose()
        {
            try
            {
                if (m_rServer != null)
                    m_rServer.Close();

                m_rServer = null;
            }
            catch (Exception exception)
            {
                throw new Exception("Exception in CRClient::Dispose().", exception);
            }
        }

        #endregion // IDisposable Members

        #endregion // Methods
    }
}
