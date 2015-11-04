package org.pentaho.di.trans.steps.sftpscan;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.trans.steps.sftpscan.client.RemoteFile;
import org.pentaho.di.trans.steps.sftpscan.client.SFTPClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * The SFTP Scan main plugin class
 * This code is based on the SFTP job entry step
 */
public class SFTPScan extends BaseStep implements StepInterface {
    private static Class<?> PKG = SFTPScanMeta.class;
    private static final int DEFAULT_PORT = 22;

    private SFTPScanMeta meta;
    private SFTPScanData data;

    private String realSftpDirString;
    private String realWildcard;
    SFTPClient sftpClient = null;

    public SFTPScan(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        meta = (SFTPScanMeta) smi;
        data = (SFTPScanData) sdi;

        data.inputRowMeta = getInputRowMeta();

        if (data.inputRowMeta == null)
            data.outputRowMeta = new RowMeta();
        else
            data.outputRowMeta = data.inputRowMeta.clone();

        String realServerName = environmentSubstitute(meta.getServerName());
        String realServerPort = environmentSubstitute(meta.getServerPort());
        String realUsername = environmentSubstitute(meta.getUserName());
        String realPassword = Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(meta.getPassword()));
        realSftpDirString = environmentSubstitute(meta.getSftpDirectory());
        realWildcard = environmentSubstitute(meta.getWildcard());
        String realKeyFilename = null;
        String realPassPhrase = null;

        try {
            if (meta.isUsekeyfilename()) {
                realKeyFilename = environmentSubstitute(meta.getKeyfilename());
                if (Const.isEmpty(realKeyFilename)) {
                    logError(BaseMessages.getString(PKG, "SftpScan.Error.KeyFileMissing"));
                    return false;
                }

                if (!KettleVFS.fileExists(realKeyFilename)) {
                    logError(BaseMessages.getString(PKG, "SftpScan.Error.KeyFileNotFound", realKeyFilename));
                    return false;
                }

                realPassPhrase = environmentSubstitute(meta.getKeyfilepass());
            }

            // Create sftp client to host ...
            sftpClient =
                    new SFTPClient(
                            InetAddress.getByName(realServerName), Const.toInt(realServerPort, DEFAULT_PORT), realUsername,
                            realKeyFilename, realPassPhrase);
            if (log.isDetailed()) {
                logDetailed(BaseMessages.getString(
                        PKG, "SftpScan.Log.OpenedConnection", realServerName, realServerPort, realUsername));
            }

            // Set compression
            sftpClient.setCompression(meta.getCompression());

            // Set proxy?
            String realProxyHost = environmentSubstitute(meta.getProxyHost());
            if (!Const.isEmpty(realProxyHost)) {
                // Set proxy
                sftpClient.setProxy(
                        realProxyHost, environmentSubstitute(meta.getProxyPort()), environmentSubstitute(meta.getProxyUsername()),
                        environmentSubstitute(meta.getProxyPassword()), meta.getProxyType());
            }

            sftpClient.login(realPassword);
        } catch (Exception e) {
            logError(BaseMessages.getString(PKG, "SftpScan.Error.CannotInitializeSftpConnection"), e);
            return false;
        }

        return super.init(smi, sdi);
    }

    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        List<RemoteFile> filelist = new ArrayList<>();

        if (!Const.isEmpty(realSftpDirString)) {
            try {
                filelist = sftpClient.ls(realSftpDirString, realWildcard, meta.isDoRecursiveScan());
            } catch (Exception e) {
                logError(BaseMessages.getString(PKG, "SftpScan.Error.CanNotFindRemoteFolder", realSftpDirString));
                throw new KettleException(e);
            }
            if (log.isDetailed()) {
                logDetailed(BaseMessages.getString(PKG, "SftpScan.Log.ChangedDirectory", realSftpDirString));
            }
        }

        if (filelist.isEmpty()) {
            if (log.isDetailed()) {
                logDetailed(BaseMessages.getString(PKG, "SftpScan.Log.Found", "" + 0));
            }

            setOutputDone();
            return false;
        }

        if (log.isDetailed()) {
            logDetailed(BaseMessages.getString(PKG, "SftpScan.Log.Found", "" + filelist.size()));
        }

        for (RemoteFile remoteFile : filelist) {
            int idx = 0;
            Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());

            outputRow[idx++] = remoteFile.getPath();
            outputRow[idx++] = remoteFile.getName();
            outputRow[idx++] = remoteFile.getPermissions();
            outputRow[idx++] = remoteFile.getSize();
            outputRow[idx++] = Long.valueOf(remoteFile.getUid());
            outputRow[idx++] = Long.valueOf(remoteFile.getGid());
            outputRow[idx++] = remoteFile.getAccessDate();
            outputRow[idx++] = remoteFile.getModificationDate();

            putRow(data.outputRowMeta, outputRow);
        }

        setOutputDone();
        return false;
    }

    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        sftpClient.disconnect();

        super.dispose(smi, sdi);
    }
}
