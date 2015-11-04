package org.pentaho.di.trans.steps.sftpscan;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

/**
 * The SFTP Plugin meta class
 */
@Step(
        id = "SftpScanStep",
        image = "icon.png",
        i18nPackageName = "org.pentaho.di.trans.steps.sftpscan",
        name = "SftpScan.Name.Default",
        description = "SftpScan.Name.Desc",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Utility"
)
public class SFTPScanMeta extends BaseStepMeta implements StepMetaInterface {

    private String serverName;
    private String serverPort;
    private String userName;
    private String password;
    private String sftpDirectory;
    private String wildcard;
    private boolean usekeyfilename;
    private boolean doRecursiveScan;
    private String keyfilename;
    private String keyfilepass;
    private String compression;

    // proxy
    private String proxyType;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    @Override
    public void setDefault() {
        serverPort = "22";
        usekeyfilename = false;
        compression = "none";
        doRecursiveScan = false;
    }

    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
        return new SFTPScan(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    @Override
    public StepDataInterface getStepData() {
        return new SFTPScanData();
    }

    @Override
    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {
        RowMetaInterface fields = new RowMeta();

        ValueMetaInterface fileFolder = new ValueMeta("file_folder", ValueMeta.TYPE_STRING);
        fileFolder.setLength(255);
        fileFolder.setPrecision(-1);
        fileFolder.setOrigin(name);
        fields.addValueMeta(fileFolder);

        ValueMetaInterface fileName = new ValueMeta("file_name", ValueMeta.TYPE_STRING);
        fileName.setLength(255);
        fileName.setPrecision(-1);
        fileName.setOrigin(name);
        fields.addValueMeta(fileName);

        ValueMetaInterface permissions = new ValueMeta("permissions", ValueMeta.TYPE_STRING);
        permissions.setLength(8);
        permissions.setPrecision(-1);
        permissions.setOrigin(name);
        fields.addValueMeta(permissions);

        ValueMetaInterface size = new ValueMeta("size", ValueMeta.TYPE_INTEGER);
        size.setOrigin(name);
        fields.addValueMeta(size);

        ValueMetaInterface uid = new ValueMeta("uid", ValueMeta.TYPE_INTEGER);
        uid.setOrigin(name);
        fields.addValueMeta(uid);

        ValueMetaInterface gid = new ValueMeta("gid", ValueMeta.TYPE_INTEGER);
        gid.setOrigin(name);
        fields.addValueMeta(gid);

        ValueMetaInterface access_date = new ValueMeta("access_date", ValueMeta.TYPE_DATE);
        access_date.setOrigin(name);
        fields.addValueMeta(access_date);


        ValueMetaInterface modification_date = new ValueMeta("modification_date", ValueMeta.TYPE_DATE);
        modification_date.setOrigin(name);
        fields.addValueMeta(modification_date);

        inputRowMeta.clear();
        inputRowMeta.addRowMeta(fields);
    }

    @Override
    public String getXML() throws KettleException {
        StringBuffer retval = new StringBuffer(200);

        retval.append(super.getXML());

        retval.append("      ").append(XMLHandler.addTagValue("servername", serverName));
        retval.append("      ").append(XMLHandler.addTagValue("serverport", serverPort));
        retval.append("      ").append(XMLHandler.addTagValue("username", userName));
        retval.append("      ").append(
                XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(getPassword())));
        retval.append("      ").append(XMLHandler.addTagValue("sftpdirectory", sftpDirectory));
        retval.append("      ").append(XMLHandler.addTagValue("wildcard", wildcard));
        retval.append("      ").append(XMLHandler.addTagValue("usekeyfilename", usekeyfilename));
        retval.append("      ").append(XMLHandler.addTagValue("doRecursiveScan", doRecursiveScan));
        retval.append("      ").append(XMLHandler.addTagValue("keyfilename", keyfilename));
        retval.append("      ").append(
                XMLHandler.addTagValue("keyfilepass", Encr.encryptPasswordIfNotUsingVariables(keyfilepass)));
        retval.append("      ").append(XMLHandler.addTagValue("compression", compression));

        retval.append("      ").append(XMLHandler.addTagValue("proxyType", proxyType));
        retval.append("      ").append(XMLHandler.addTagValue("proxyHost", proxyHost));
        retval.append("      ").append(XMLHandler.addTagValue("proxyPort", proxyPort));
        retval.append("      ").append(XMLHandler.addTagValue("proxyUsername", proxyUsername));
        retval.append("      ").append(
                XMLHandler.addTagValue("proxyPassword", Encr.encryptPasswordIfNotUsingVariables(proxyPassword)));

        return retval.toString();
    }

    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        try {
            super.loadXML(stepnode, databases, metaStore);
            serverName = XMLHandler.getTagValue(stepnode, "servername");
            serverPort = XMLHandler.getTagValue(stepnode, "serverport");
            userName = XMLHandler.getTagValue(stepnode, "username");
            password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password"));
            sftpDirectory = XMLHandler.getTagValue(stepnode, "sftpdirectory");
            wildcard = XMLHandler.getTagValue(stepnode, "wildcard");
            usekeyfilename = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usekeyfilename"));
            doRecursiveScan = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "doRecursiveScan"));
            keyfilename = XMLHandler.getTagValue(stepnode, "keyfilename");
            keyfilepass = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "keyfilepass"));
            compression = XMLHandler.getTagValue(stepnode, "compression");
            proxyType = XMLHandler.getTagValue(stepnode, "proxyType");
            proxyHost = XMLHandler.getTagValue(stepnode, "proxyHost");
            proxyPort = XMLHandler.getTagValue(stepnode, "proxyPort");
            proxyUsername = XMLHandler.getTagValue(stepnode, "proxyUsername");
            proxyPassword =
                    Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "proxyPassword"));
        } catch (KettleXMLException xe) {
            throw new KettleXMLException("Unable to load job entry of type 'SftpScanStep' from XML node", xe);
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSftpDirectory() {
        return sftpDirectory;
    }

    public void setSftpDirectory(String sftpDirectory) {
        this.sftpDirectory = sftpDirectory;
    }

    public String getWildcard() {
        return wildcard;
    }

    public void setWildcard(String wildcard) {
        this.wildcard = wildcard;
    }

    public boolean isUsekeyfilename() {
        return usekeyfilename;
    }

    public void setUsekeyfilename(boolean usekeyfilename) {
        this.usekeyfilename = usekeyfilename;
    }

    public boolean isDoRecursiveScan() {
        return doRecursiveScan;
    }

    public void setDoRecursiveScan(boolean doRecursiveScan) {
        this.doRecursiveScan = doRecursiveScan;
    }

    public String getKeyfilename() {
        return keyfilename;
    }

    public void setKeyfilename(String keyfilename) {
        this.keyfilename = keyfilename;
    }

    public String getKeyfilepass() {
        return keyfilepass;
    }

    public void setKeyfilepass(String keyfilepass) {
        this.keyfilepass = keyfilepass;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
}
