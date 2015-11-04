package org.pentaho.di.ui.trans.steps.sftpscan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.sftpscan.SFTPScanMeta;
import org.pentaho.di.trans.steps.sftpscan.client.SFTPClient;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.PasswordTextVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.net.InetAddress;

/**
 * The SFTP Scan Dialog class
 * Heavily based on the SFTP job entry dialog
 */
public class SFTPScanDialog extends BaseStepDialog implements StepDialogInterface {
    private static Class<?> PKG = SFTPScanMeta.class;

    private static final String[] FILETYPES =
            new String[]{
                    BaseMessages.getString(PKG, "SftpScan.Filetype.Pem"),
                    BaseMessages.getString(PKG, "SftpScan.Filetype.All")
            };

    private Label wlName;

    private Text wName;

    private FormData fdlName, fdName;

    private Label wlServerName;

    private TextVar wServerName;

    private FormData fdlServerName, fdServerName;

    private Label wlServerPort;

    private TextVar wServerPort;

    private FormData fdlServerPort, fdServerPort;

    private Label wlUserName;

    private TextVar wUserName;

    private FormData fdlUserName, fdUserName;

    private Label wlPassword;

    private TextVar wPassword;

    private FormData fdlPassword, fdPassword;

    private Label wlScpDirectory;

    private TextVar wScpDirectory;

    private FormData fdlScpDirectory, fdScpDirectory;

    private Label wlWildcard;

    private TextVar wWildcard;

    private FormData fdlWildcard, fdWildcard;

    private Button wOK, wCancel;

    private Listener lsOK, lsCancel;

    private Listener lsCheckChangeFolder;

    private SFTPScanMeta sftpScanMeta;

    private Shell shell;

    private SelectionAdapter lsDef;

    private boolean changed;

    private Button wTest;

    private FormData fdTest;

    private Listener lsTest;

    private Button wbTestChangeFolderExists;

    private FormData fdbTestChangeFolderExists;

    private Group wServerSettings;

    private FormData fdServerSettings;

    private Group wSourceFiles;

    private FormData fdSourceFiles;

    private Label wldoRecursiveScan;

    private Button wdoRecursiveScan;

    private FormData fdldoRecursiveScan, fddoRecursiveScan;

    private LabelTextVar wkeyfilePass;

    private FormData fdkeyfilePass;

    private Label wlusePublicKey;

    private Button wusePublicKey;

    private FormData fdlusePublicKey, fdusePublicKey;

    private Label wlKeyFilename;

    private Button wbKeyFilename;

    private TextVar wKeyFilename;

    private FormData fdlKeyFilename, fdbKeyFilename, fdKeyFilename;

    private CTabFolder wTabFolder;
    private Composite wGeneralComp, wFilesComp;
    private CTabItem wGeneralTab, wFilesTab;
    private FormData fdGeneralComp, fdFilesComp;
    private FormData fdTabFolder;

    private SFTPClient sftpclient = null;

    private Label wlCompression;
    private FormData fdlCompression;
    private CCombo wCompression;
    private FormData fdCompression;

    private Label wlProxyType;
    private FormData fdlProxyType;
    private CCombo wProxyType;
    private FormData fdProxyType;

    private LabelTextVar wProxyHost;
    private FormData fdProxyHost;
    private LabelTextVar wProxyPort;
    private FormData fdProxyPort;
    private LabelTextVar wProxyUsername;
    private FormData fdProxyUsername;
    private LabelTextVar wProxyPassword;
    private FormData fdProxyPasswd;

    public SFTPScanDialog(Shell parent, Object in, TransMeta transMeta, String stepname) {
        super(parent, (BaseStepMeta) in, transMeta, stepname);
        sftpScanMeta = (SFTPScanMeta) in;
    }

    @Override
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        setShellImage(shell, sftpScanMeta);

        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                sftpclient = null;
                sftpScanMeta.setChanged();
            }
        };
        changed = sftpScanMeta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "SftpScan.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Filename line
        wlName = new Label(shell, SWT.RIGHT);
        wlName.setText(BaseMessages.getString(PKG, "SftpScan.Name.Label"));
        props.setLook(wlName);
        fdlName = new FormData();
        fdlName.left = new FormAttachment(0, 0);
        fdlName.right = new FormAttachment(middle, -margin);
        fdlName.top = new FormAttachment(0, margin);
        wlName.setLayoutData(fdlName);
        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wName.setText(stepname);
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.left = new FormAttachment(middle, 0);
        fdName.top = new FormAttachment(0, margin);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);

        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

        // ////////////////////////
        // START OF GENERAL TAB ///
        // ////////////////////////

        wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
        wGeneralTab.setText(BaseMessages.getString(PKG, "SftpScan.Tab.General.Label"));

        wGeneralComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wGeneralComp);

        FormLayout generalLayout = new FormLayout();
        generalLayout.marginWidth = 3;
        generalLayout.marginHeight = 3;
        wGeneralComp.setLayout(generalLayout);

        // ////////////////////////
        // START OF SERVER SETTINGS GROUP///
        // /
        wServerSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
        props.setLook(wServerSettings);
        wServerSettings.setText(BaseMessages.getString(PKG, "SftpScan.ServerSettings.Group.Label"));
        FormLayout ServerSettingsgroupLayout = new FormLayout();
        ServerSettingsgroupLayout.marginWidth = 10;
        ServerSettingsgroupLayout.marginHeight = 10;
        wServerSettings.setLayout(ServerSettingsgroupLayout);

        // ServerName line
        wlServerName = new Label(wServerSettings, SWT.RIGHT);
        wlServerName.setText(BaseMessages.getString(PKG, "SftpScan.Server.Label"));
        props.setLook(wlServerName);
        fdlServerName = new FormData();
        fdlServerName.left = new FormAttachment(0, 0);
        fdlServerName.top = new FormAttachment(wName, margin);
        fdlServerName.right = new FormAttachment(middle, -margin);
        wlServerName.setLayoutData(fdlServerName);
        wServerName = new TextVar(transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerName);
        wServerName.addModifyListener(lsMod);
        fdServerName = new FormData();
        fdServerName.left = new FormAttachment(middle, 0);
        fdServerName.top = new FormAttachment(wName, margin);
        fdServerName.right = new FormAttachment(100, 0);
        wServerName.setLayoutData(fdServerName);

        // ServerPort line
        wlServerPort = new Label(wServerSettings, SWT.RIGHT);
        wlServerPort.setText(BaseMessages.getString(PKG, "SftpScan.Port.Label"));
        props.setLook(wlServerPort);
        fdlServerPort = new FormData();
        fdlServerPort.left = new FormAttachment(0, 0);
        fdlServerPort.top = new FormAttachment(wServerName, margin);
        fdlServerPort.right = new FormAttachment(middle, -margin);
        wlServerPort.setLayoutData(fdlServerPort);
        wServerPort = new TextVar(transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wServerPort);
        wServerPort.setToolTipText(BaseMessages.getString(PKG, "SftpScan.Port.Tooltip"));
        wServerPort.addModifyListener(lsMod);
        fdServerPort = new FormData();
        fdServerPort.left = new FormAttachment(middle, 0);
        fdServerPort.top = new FormAttachment(wServerName, margin);
        fdServerPort.right = new FormAttachment(100, 0);
        wServerPort.setLayoutData(fdServerPort);

        // UserName line
        wlUserName = new Label(wServerSettings, SWT.RIGHT);
        wlUserName.setText(BaseMessages.getString(PKG, "SftpScan.Username.Label"));
        props.setLook(wlUserName);
        fdlUserName = new FormData();
        fdlUserName.left = new FormAttachment(0, 0);
        fdlUserName.top = new FormAttachment(wServerPort, margin);
        fdlUserName.right = new FormAttachment(middle, -margin);
        wlUserName.setLayoutData(fdlUserName);
        wUserName = new TextVar(transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wUserName);
        wUserName.addModifyListener(lsMod);
        fdUserName = new FormData();
        fdUserName.left = new FormAttachment(middle, 0);
        fdUserName.top = new FormAttachment(wServerPort, margin);
        fdUserName.right = new FormAttachment(100, 0);
        wUserName.setLayoutData(fdUserName);

        // Password line
        wlPassword = new Label(wServerSettings, SWT.RIGHT);
        wlPassword.setText(BaseMessages.getString(PKG, "SftpScan.Password.Label"));
        props.setLook(wlPassword);
        fdlPassword = new FormData();
        fdlPassword.left = new FormAttachment(0, 0);
        fdlPassword.top = new FormAttachment(wUserName, margin);
        fdlPassword.right = new FormAttachment(middle, -margin);
        wlPassword.setLayoutData(fdlPassword);
        wPassword = new PasswordTextVar(transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wPassword);
        wPassword.addModifyListener(lsMod);
        fdPassword = new FormData();
        fdPassword.left = new FormAttachment(middle, 0);
        fdPassword.top = new FormAttachment(wUserName, margin);
        fdPassword.right = new FormAttachment(100, 0);
        wPassword.setLayoutData(fdPassword);

        // usePublicKey
        wlusePublicKey = new Label(wServerSettings, SWT.RIGHT);
        wlusePublicKey.setText(BaseMessages.getString(PKG, "SftpScan.useKeyFile.Label"));
        props.setLook(wlusePublicKey);
        fdlusePublicKey = new FormData();
        fdlusePublicKey.left = new FormAttachment(0, 0);
        fdlusePublicKey.top = new FormAttachment(wPassword, margin);
        fdlusePublicKey.right = new FormAttachment(middle, -margin);
        wlusePublicKey.setLayoutData(fdlusePublicKey);
        wusePublicKey = new Button(wServerSettings, SWT.CHECK);
        wusePublicKey.setToolTipText(BaseMessages.getString(PKG, "SftpScan.useKeyFile.Tooltip"));
        props.setLook(wusePublicKey);
        fdusePublicKey = new FormData();
        fdusePublicKey.left = new FormAttachment(middle, 0);
        fdusePublicKey.top = new FormAttachment(wPassword, margin);
        fdusePublicKey.right = new FormAttachment(100, 0);
        wusePublicKey.setLayoutData(fdusePublicKey);
        wusePublicKey.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                activeUseKey();
                sftpScanMeta.setChanged();
            }
        });

        // Key File
        wlKeyFilename = new Label(wServerSettings, SWT.RIGHT);
        wlKeyFilename.setText(BaseMessages.getString(PKG, "SftpScan.KeyFilename.Label"));
        props.setLook(wlKeyFilename);
        fdlKeyFilename = new FormData();
        fdlKeyFilename.left = new FormAttachment(0, 0);
        fdlKeyFilename.top = new FormAttachment(wusePublicKey, margin);
        fdlKeyFilename.right = new FormAttachment(middle, -margin);
        wlKeyFilename.setLayoutData(fdlKeyFilename);

        wbKeyFilename = new Button(wServerSettings, SWT.PUSH | SWT.CENTER);
        props.setLook(wbKeyFilename);
        wbKeyFilename.setText(BaseMessages.getString(PKG, "System.Button.Browse"));
        fdbKeyFilename = new FormData();
        fdbKeyFilename.right = new FormAttachment(100, 0);
        fdbKeyFilename.top = new FormAttachment(wusePublicKey, 0);
        // fdbKeyFilename.height = 22;
        wbKeyFilename.setLayoutData(fdbKeyFilename);

        wKeyFilename = new TextVar(transMeta, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wKeyFilename.setToolTipText(BaseMessages.getString(PKG, "SftpScan.KeyFilename.Tooltip"));
        props.setLook(wKeyFilename);
        wKeyFilename.addModifyListener(lsMod);
        fdKeyFilename = new FormData();
        fdKeyFilename.left = new FormAttachment(middle, 0);
        fdKeyFilename.top = new FormAttachment(wusePublicKey, margin);
        fdKeyFilename.right = new FormAttachment(wbKeyFilename, -margin);
        wKeyFilename.setLayoutData(fdKeyFilename);

        // Whenever something changes, set the tooltip to the expanded version:
        wbKeyFilename.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterExtensions(new String[]{"*.pem", "*"});
                if (wKeyFilename.getText() != null) {
                    dialog.setFileName(transMeta.environmentSubstitute(wKeyFilename.getText()));
                }
                dialog.setFilterNames(FILETYPES);
                if (dialog.open() != null) {
                    wKeyFilename.setText(dialog.getFilterPath() + Const.FILE_SEPARATOR + dialog.getFileName());
                }
            }
        });

        // keyfilePass line
        wkeyfilePass =
                new LabelTextVar(
                        transMeta, wServerSettings, BaseMessages.getString(PKG, "SftpScan.keyfilePass.Label"), BaseMessages
                        .getString(PKG, "SftpScan.keyfilePass.Tooltip"), true);
        props.setLook(wkeyfilePass);
        wkeyfilePass.addModifyListener(lsMod);
        fdkeyfilePass = new FormData();
        fdkeyfilePass.left = new FormAttachment(0, -2 * margin);
        fdkeyfilePass.top = new FormAttachment(wKeyFilename, margin);
        fdkeyfilePass.right = new FormAttachment(100, 0);
        wkeyfilePass.setLayoutData(fdkeyfilePass);

        wlProxyType = new Label(wServerSettings, SWT.RIGHT);
        wlProxyType.setText(BaseMessages.getString(PKG, "SftpScan.ProxyType.Label"));
        props.setLook(wlProxyType);
        fdlProxyType = new FormData();
        fdlProxyType.left = new FormAttachment(0, 0);
        fdlProxyType.right = new FormAttachment(middle, -margin);
        fdlProxyType.top = new FormAttachment(wkeyfilePass, 2 * margin);
        wlProxyType.setLayoutData(fdlProxyType);

        wProxyType = new CCombo(wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wProxyType.add(SFTPClient.PROXY_TYPE_HTTP);
        wProxyType.add(SFTPClient.PROXY_TYPE_SOCKS5);
        wProxyType.select(0); // +1: starts at -1
        props.setLook(wProxyType);
        fdProxyType = new FormData();
        fdProxyType.left = new FormAttachment(middle, 0);
        fdProxyType.top = new FormAttachment(wkeyfilePass, 2 * margin);
        fdProxyType.right = new FormAttachment(100, 0);
        wProxyType.setLayoutData(fdProxyType);
        wProxyType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                setDefaultProxyPort();
            }
        });

        // Proxy host line
        wProxyHost =
                new LabelTextVar(
                        transMeta, wServerSettings, BaseMessages.getString(PKG, "SftpScan.ProxyHost.Label"), BaseMessages
                        .getString(PKG, "SftpScan.ProxyHost.Tooltip"));
        props.setLook(wProxyHost);
        wProxyHost.addModifyListener(lsMod);
        fdProxyHost = new FormData();
        fdProxyHost.left = new FormAttachment(0, -2 * margin);
        fdProxyHost.top = new FormAttachment(wProxyType, margin);
        fdProxyHost.right = new FormAttachment(100, 0);
        wProxyHost.setLayoutData(fdProxyHost);

        // Proxy port line
        wProxyPort =
                new LabelTextVar(
                        transMeta, wServerSettings, BaseMessages.getString(PKG, "SftpScan.ProxyPort.Label"), BaseMessages
                        .getString(PKG, "SftpScan.ProxyPort.Tooltip"));
        props.setLook(wProxyPort);
        wProxyPort.addModifyListener(lsMod);
        fdProxyPort = new FormData();
        fdProxyPort.left = new FormAttachment(0, -2 * margin);
        fdProxyPort.top = new FormAttachment(wProxyHost, margin);
        fdProxyPort.right = new FormAttachment(100, 0);
        wProxyPort.setLayoutData(fdProxyPort);

        // Proxy username line
        wProxyUsername =
                new LabelTextVar(
                        transMeta, wServerSettings, BaseMessages.getString(PKG, "SftpScan.ProxyUsername.Label"), BaseMessages
                        .getString(PKG, "SftpScan.ProxyUsername.Tooltip"));
        props.setLook(wProxyUsername);
        wProxyUsername.addModifyListener(lsMod);
        fdProxyUsername = new FormData();
        fdProxyUsername.left = new FormAttachment(0, -2 * margin);
        fdProxyUsername.top = new FormAttachment(wProxyPort, margin);
        fdProxyUsername.right = new FormAttachment(100, 0);
        wProxyUsername.setLayoutData(fdProxyUsername);

        // Proxy password line
        wProxyPassword =
                new LabelTextVar(
                        transMeta, wServerSettings, BaseMessages.getString(PKG, "SftpScan.ProxyPassword.Label"), BaseMessages
                        .getString(PKG, "SftpScan.ProxyPassword.Tooltip"), true);
        props.setLook(wProxyPassword);
        wProxyPassword.addModifyListener(lsMod);
        fdProxyPasswd = new FormData();
        fdProxyPasswd.left = new FormAttachment(0, -2 * margin);
        fdProxyPasswd.top = new FormAttachment(wProxyUsername, margin);
        fdProxyPasswd.right = new FormAttachment(100, 0);
        wProxyPassword.setLayoutData(fdProxyPasswd);

        // Test connection button
        wTest = new Button(wServerSettings, SWT.PUSH);
        wTest.setText(BaseMessages.getString(PKG, "SftpScan.TestConnection.Label"));
        props.setLook(wTest);
        fdTest = new FormData();
        wTest.setToolTipText(BaseMessages.getString(PKG, "SftpScan.TestConnection.Tooltip"));
        fdTest.top = new FormAttachment(wProxyPassword, margin);
        fdTest.right = new FormAttachment(100, 0);
        wTest.setLayoutData(fdTest);

        fdServerSettings = new FormData();
        fdServerSettings.left = new FormAttachment(0, margin);
        fdServerSettings.top = new FormAttachment(wName, margin);
        fdServerSettings.right = new FormAttachment(100, -margin);
        wServerSettings.setLayoutData(fdServerSettings);
        // ///////////////////////////////////////////////////////////
        // / END OF SERVER SETTINGS GROUP
        // ///////////////////////////////////////////////////////////

        wlCompression = new Label(wGeneralComp, SWT.RIGHT);
        wlCompression.setText(BaseMessages.getString(PKG, "SftpScan.Compression.Label"));
        props.setLook(wlCompression);
        fdlCompression = new FormData();
        fdlCompression.left = new FormAttachment(0, -margin);
        fdlCompression.right = new FormAttachment(middle, 0);
        fdlCompression.top = new FormAttachment(wServerSettings, margin);
        wlCompression.setLayoutData(fdlCompression);

        wCompression = new CCombo(wGeneralComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        wCompression.add("none");
        wCompression.add("zlib");
        wCompression.select(0); // +1: starts at -1

        props.setLook(wCompression);
        fdCompression = new FormData();
        fdCompression.left = new FormAttachment(middle, margin);
        fdCompression.top = new FormAttachment(wServerSettings, margin);
        fdCompression.right = new FormAttachment(100, 0);
        wCompression.setLayoutData(fdCompression);

        fdGeneralComp = new FormData();
        fdGeneralComp.left = new FormAttachment(0, 0);
        fdGeneralComp.top = new FormAttachment(0, 0);
        fdGeneralComp.right = new FormAttachment(100, 0);
        fdGeneralComp.bottom = new FormAttachment(100, 0);
        wGeneralComp.setLayoutData(fdGeneralComp);

        wGeneralComp.layout();
        wGeneralTab.setControl(wGeneralComp);
        props.setLook(wGeneralComp);

        // ///////////////////////////////////////////////////////////
        // / END OF GENERAL TAB
        // ///////////////////////////////////////////////////////////

        // ////////////////////////
        // START OF Files TAB ///
        // ////////////////////////

        wFilesTab = new CTabItem(wTabFolder, SWT.NONE);
        wFilesTab.setText(BaseMessages.getString(PKG, "SftpScan.Tab.Files.Label"));

        wFilesComp = new Composite(wTabFolder, SWT.NONE);
        props.setLook(wFilesComp);

        FormLayout FilesLayout = new FormLayout();
        FilesLayout.marginWidth = 3;
        FilesLayout.marginHeight = 3;
        wFilesComp.setLayout(FilesLayout);

        // ////////////////////////
        // START OF Source files GROUP///
        // /
        wSourceFiles = new Group(wFilesComp, SWT.SHADOW_NONE);
        props.setLook(wSourceFiles);
        wSourceFiles.setText(BaseMessages.getString(PKG, "SftpScan.SourceFiles.Group.Label"));
        FormLayout SourceFilesgroupLayout = new FormLayout();
        SourceFilesgroupLayout.marginWidth = 10;
        SourceFilesgroupLayout.marginHeight = 10;
        wSourceFiles.setLayout(SourceFilesgroupLayout);

        // Get arguments from previous result...
        wldoRecursiveScan = new Label(wSourceFiles, SWT.RIGHT);
        wldoRecursiveScan.setText(BaseMessages.getString(PKG, "SftpScan.doRecursiveScan.Label"));
        props.setLook(wldoRecursiveScan);
        fdldoRecursiveScan = new FormData();
        fdldoRecursiveScan.left = new FormAttachment(0, 0);
        fdldoRecursiveScan.top = new FormAttachment(wServerSettings, 2 * margin);
        fdldoRecursiveScan.right = new FormAttachment(middle, -margin);
        wldoRecursiveScan.setLayoutData(fdldoRecursiveScan);
        wdoRecursiveScan = new Button(wSourceFiles, SWT.CHECK);
        props.setLook(wdoRecursiveScan);
        wdoRecursiveScan.setToolTipText(BaseMessages.getString(PKG, "SftpScan.doRecursiveScan.Tooltip"));
        fddoRecursiveScan = new FormData();
        fddoRecursiveScan.left = new FormAttachment(middle, 0);
        fddoRecursiveScan.top = new FormAttachment(wServerSettings, 2 * margin);
        fddoRecursiveScan.right = new FormAttachment(100, 0);
        wdoRecursiveScan.setLayoutData(fddoRecursiveScan);
        wdoRecursiveScan.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                sftpScanMeta.setChanged();
            }
        });

        // FtpDirectory line
        wlScpDirectory = new Label(wSourceFiles, SWT.RIGHT);
        wlScpDirectory.setText(BaseMessages.getString(PKG, "SftpScan.RemoteDir.Label"));
        props.setLook(wlScpDirectory);
        fdlScpDirectory = new FormData();
        fdlScpDirectory.left = new FormAttachment(0, 0);
        fdlScpDirectory.top = new FormAttachment(wdoRecursiveScan, 2 * margin);
        fdlScpDirectory.right = new FormAttachment(middle, -margin);
        wlScpDirectory.setLayoutData(fdlScpDirectory);

        // Test remote folder button ...
        wbTestChangeFolderExists = new Button(wSourceFiles, SWT.PUSH | SWT.CENTER);
        props.setLook(wbTestChangeFolderExists);
        wbTestChangeFolderExists.setText(BaseMessages.getString(PKG, "SftpScan.TestFolderExists.Label"));
        fdbTestChangeFolderExists = new FormData();
        fdbTestChangeFolderExists.right = new FormAttachment(100, 0);
        fdbTestChangeFolderExists.top = new FormAttachment(wdoRecursiveScan, 2 * margin);
        wbTestChangeFolderExists.setLayoutData(fdbTestChangeFolderExists);

        wScpDirectory =
                new TextVar(transMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
                        PKG, "SftpScan.RemoteDir.Tooltip"));
        props.setLook(wScpDirectory);
        wScpDirectory.addModifyListener(lsMod);
        fdScpDirectory = new FormData();
        fdScpDirectory.left = new FormAttachment(middle, 0);
        fdScpDirectory.top = new FormAttachment(wdoRecursiveScan, 2 * margin);
        fdScpDirectory.right = new FormAttachment(wbTestChangeFolderExists, -margin);
        wScpDirectory.setLayoutData(fdScpDirectory);

        // Wildcard line
        wlWildcard = new Label(wSourceFiles, SWT.RIGHT);
        wlWildcard.setText(BaseMessages.getString(PKG, "SftpScan.Wildcard.Label"));
        props.setLook(wlWildcard);
        fdlWildcard = new FormData();
        fdlWildcard.left = new FormAttachment(0, 0);
        fdlWildcard.top = new FormAttachment(wScpDirectory, margin);
        fdlWildcard.right = new FormAttachment(middle, -margin);
        wlWildcard.setLayoutData(fdlWildcard);
        wWildcard =
                new TextVar(transMeta, wSourceFiles, SWT.SINGLE | SWT.LEFT | SWT.BORDER, BaseMessages.getString(
                        PKG, "SftpScan.Wildcard.Tooltip"));
        props.setLook(wWildcard);
        wWildcard.addModifyListener(lsMod);
        fdWildcard = new FormData();
        fdWildcard.left = new FormAttachment(middle, 0);
        fdWildcard.top = new FormAttachment(wScpDirectory, margin);
        fdWildcard.right = new FormAttachment(100, 0);
        wWildcard.setLayoutData(fdWildcard);

        fdSourceFiles = new FormData();
        fdSourceFiles.left = new FormAttachment(0, margin);
        fdSourceFiles.top = new FormAttachment(wServerSettings, 2 * margin);
        fdSourceFiles.right = new FormAttachment(100, -margin);
        wSourceFiles.setLayoutData(fdSourceFiles);
        // ///////////////////////////////////////////////////////////
        // / END OF Source files GROUP
        // ///////////////////////////////////////////////////////////

        fdFilesComp = new FormData();
        fdFilesComp.left = new FormAttachment(0, 0);
        fdFilesComp.top = new FormAttachment(0, 0);
        fdFilesComp.right = new FormAttachment(100, 0);
        fdFilesComp.bottom = new FormAttachment(100, 0);
        wFilesComp.setLayoutData(fdFilesComp);

        wFilesComp.layout();
        wFilesTab.setControl(wFilesComp);
        props.setLook(wFilesComp);

        // ///////////////////////////////////////////////////////////
        // / END OF Files TAB
        // ///////////////////////////////////////////////////////////

        fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wName, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[]{wOK, wCancel}, margin, wTabFolder);

        // Add listeners
        lsCancel = new Listener() {
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            public void handleEvent(Event e) {
                ok();
            }
        };
        lsTest = new Listener() {
            public void handleEvent(Event e) {
                test();
            }
        };
        lsCheckChangeFolder = new Listener() {
            public void handleEvent(Event e) {
                checkRemoteFolder();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wTest.addListener(SWT.Selection, lsTest);
        wbTestChangeFolderExists.addListener(SWT.Selection, lsCheckChangeFolder);

        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wServerName.addSelectionListener(lsDef);
        wUserName.addSelectionListener(lsDef);
        wPassword.addSelectionListener(lsDef);
        wScpDirectory.addSelectionListener(lsDef);
        wWildcard.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        getData();
        activeUseKey();

        BaseStepDialog.setSize(shell);
        wTabFolder.setSelection(0);
        shell.open();
        props.setDialogSize(shell, "StepSFTPScanDialogSize");
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return stepname;
    }

    @Override
    public void dispose() {
        closeFTPConnections();
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    public void getData() {
        wServerName.setText(Const.NVL(sftpScanMeta.getServerName(), ""));
        wServerPort.setText(sftpScanMeta.getServerPort());
        wUserName.setText(Const.NVL(sftpScanMeta.getUserName(), ""));
        wPassword.setText(Const.NVL(sftpScanMeta.getPassword(), ""));
        wScpDirectory.setText(Const.NVL(sftpScanMeta.getSftpDirectory(), ""));
        wWildcard.setText(Const.NVL(sftpScanMeta.getWildcard(), ""));
        wdoRecursiveScan.setSelection(sftpScanMeta.isDoRecursiveScan());
        wusePublicKey.setSelection(sftpScanMeta.isUsekeyfilename());
        wKeyFilename.setText(Const.NVL(sftpScanMeta.getKeyfilename(), ""));
        wkeyfilePass.setText(Const.NVL(sftpScanMeta.getKeyfilepass(), ""));
        wCompression.setText(Const.NVL(sftpScanMeta.getCompression(), "none"));
        wProxyType.setText(Const.NVL(sftpScanMeta.getProxyType(), ""));
        wProxyHost.setText(Const.NVL(sftpScanMeta.getProxyHost(), ""));
        wProxyPort.setText(Const.NVL(sftpScanMeta.getProxyPort(), ""));
        wProxyUsername.setText(Const.NVL(sftpScanMeta.getProxyUsername(), ""));
        wProxyPassword.setText(Const.NVL(sftpScanMeta.getProxyPassword(), ""));

        wName.selectAll();
        wName.setFocus();
    }

    private void cancel() {
        sftpScanMeta.setChanged(changed);
        sftpScanMeta = null;
        dispose();
    }

    private void ok() {
        if (Const.isEmpty(wName.getText())) {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
            mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
            mb.open();
            return;
        }

        stepname = wName.getText();
        sftpScanMeta.setServerName(wServerName.getText());
        sftpScanMeta.setServerPort(wServerPort.getText());
        sftpScanMeta.setUserName(wUserName.getText());
        sftpScanMeta.setPassword(wPassword.getText());
        sftpScanMeta.setSftpDirectory(wScpDirectory.getText());
        sftpScanMeta.setWildcard(wWildcard.getText());
        sftpScanMeta.setDoRecursiveScan(wdoRecursiveScan.getSelection());
        sftpScanMeta.setUsekeyfilename(wusePublicKey.getSelection());
        sftpScanMeta.setKeyfilename(wKeyFilename.getText());
        sftpScanMeta.setKeyfilepass(wkeyfilePass.getText());
        sftpScanMeta.setCompression(wCompression.getText());
        sftpScanMeta.setProxyType(wProxyType.getText());
        sftpScanMeta.setProxyHost(wProxyHost.getText());
        sftpScanMeta.setProxyPort(wProxyPort.getText());
        sftpScanMeta.setProxyUsername(wProxyUsername.getText());
        sftpScanMeta.setProxyPassword(wProxyPassword.getText());
        dispose();
    }

    private void activeUseKey() {
        wlKeyFilename.setEnabled(wusePublicKey.getSelection());
        wKeyFilename.setEnabled(wusePublicKey.getSelection());
        wbKeyFilename.setEnabled(wusePublicKey.getSelection());
        wkeyfilePass.setEnabled(wusePublicKey.getSelection());
    }

    private void test() {
        if (connectToSFTP(false, null)) {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(BaseMessages.getString(PKG, "SftpScan.Connected.OK", wServerName.getText()) + Const.CR);
            mb.setText(BaseMessages.getString(PKG, "SftpScan.Connected.Title.Ok"));
            mb.open();
        }
    }

    private void closeFTPConnections() {
        // Close SecureFTP connection if necessary
        if (sftpclient != null) {
            try {
                sftpclient.disconnect();
                sftpclient = null;
            } catch (Exception e) {
                // Ignore errors
            }
        }
    }

    private boolean connectToSFTP(boolean checkFolder, String Remotefoldername) {
        boolean retval = false;
        try {
            if (sftpclient == null) {
                // Create sftp client to host ...
                sftpclient = new SFTPClient(
                        InetAddress.getByName(transMeta.environmentSubstitute(wServerName.getText())),
                        Const.toInt(transMeta.environmentSubstitute(wServerPort.getText()), 22),
                        transMeta.environmentSubstitute(wUserName.getText()),
                        transMeta.environmentSubstitute(wKeyFilename.getText()),
                        transMeta.environmentSubstitute(wkeyfilePass.getText()));

                // Set proxy?
                String realProxyHost = transMeta.environmentSubstitute(wProxyHost.getText());
                if (!Const.isEmpty(realProxyHost)) {
                    // Set proxy
                    sftpclient.setProxy(
                            realProxyHost,
                            transMeta.environmentSubstitute(wProxyPort.getText()),
                            transMeta.environmentSubstitute(wProxyUsername.getText()),
                            transMeta.environmentSubstitute(wProxyPassword.getText()),
                            wProxyType.getText());
                }
                // login to ftp host ...
                sftpclient.login(transMeta.environmentSubstitute(wPassword.getText()));

                retval = true;
            }
            if (checkFolder) {
                retval = sftpclient.folderExists(Remotefoldername);
            }
        } catch (Exception e) {
            if (sftpclient != null) {
                try {
                    sftpclient.disconnect();
                } catch (Exception ignored) {
                    // We've tried quitting the SFTP Client exception
                    // nothing else to be done if the SFTP Client was already disconnected
                }
                sftpclient = null;
            }
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(BaseMessages.getString(PKG, "SftpScan.ErrorConnect.NOK", wServerName.getText(), e
                    .getMessage())
                    + Const.CR);
            mb.setText(BaseMessages.getString(PKG, "SftpScan.ErrorConnect.Title.Bad"));
            mb.open();
        }
        return retval;
    }

    private void checkRemoteFolder() {
        String changeFTPFolder = transMeta.environmentSubstitute(wScpDirectory.getText());
        if (!Const.isEmpty(changeFTPFolder)) {
            if (connectToSFTP(true, changeFTPFolder)) {
                MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
                mb.setMessage(BaseMessages.getString(PKG, "SftpScan.FolderExists.OK", changeFTPFolder) + Const.CR);
                mb.setText(BaseMessages.getString(PKG, "SftpScan.FolderExists.Title.Ok"));
                mb.open();
            }
        }
    }

    private void setDefaultProxyPort() {
        if (wProxyType.getText().equals(SFTPClient.PROXY_TYPE_HTTP)) {
            if (Const.isEmpty(wProxyPort.getText())
                    || (!Const.isEmpty(wProxyPort.getText()) && wProxyPort.getText().equals(
                    SFTPClient.SOCKS5_DEFAULT_PORT))) {
                wProxyPort.setText(SFTPClient.HTTP_DEFAULT_PORT);
            }
        } else {
            if (Const.isEmpty(wProxyPort.getText())
                    || (!Const.isEmpty(wProxyPort.getText()) && wProxyPort
                    .getText().equals(SFTPClient.HTTP_DEFAULT_PORT))) {
                wProxyPort.setText(SFTPClient.SOCKS5_DEFAULT_PORT);
            }
        }
    }
}
