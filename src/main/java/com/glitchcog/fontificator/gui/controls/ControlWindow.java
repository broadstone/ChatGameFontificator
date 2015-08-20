package com.glitchcog.fontificator.gui.controls;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import com.glitchcog.fontificator.bot.ChatViewerBot;
import com.glitchcog.fontificator.bot.MessageType;
import com.glitchcog.fontificator.config.ConfigFont;
import com.glitchcog.fontificator.config.FontificatorProperties;
import com.glitchcog.fontificator.gui.chat.ChatWindow;
import com.glitchcog.fontificator.gui.component.MenuComponent;
import com.glitchcog.fontificator.gui.controls.panel.ControlTabs;

/**
 * This is the window for containing all the options of the chat display window
 * 
 * @author Matt Yanos
 */
public class ControlWindow extends JDialog
{
    private static final Logger logger = Logger.getLogger(ControlWindow.class);

    private static final long serialVersionUID = 1L;

    private final String PRESET_DIRECTORY = "presets/";

    private final String DEFAULT_FILE_EXTENSION = "cgf";

    private ControlTabs controlTabs;

    private ChatViewerBot bot;

    private ChatWindow chatWindow;

    private ManualMessageDialog manualMessageDialog;

    private FontificatorProperties fProps;

    private JEditorPane aboutPane;

    // @formatter:off
    private static String ABOUT_CONTENTS = "<html><table bgcolor=#EEEEEE width=100% border=1><tr><td>" + 
        "<center><font face=\"Arial, Helvetica\"><b>Chat Game Fontificator</b> is an Internet Relay Chat (IRC) display that<br />" + 
        "makes the chat look like the text boxes from various video games.<br /><br />" + 
        "By Matt Yanos<br /><br />" + 
        "<a href=\"www.github.com/GlitchCog/ChatGameFontificator\">www.github.com/GlitchCog/ChatGameFontificator</a>" + 
        "</font></center>" + 
        "</td></tr></table></html>";
    // @formatter:on

    private String helpText;

    public static ControlWindow me;

    private JDialog help;

    private JFileChooser opener;

    private JFileChooser saver;

    public ControlWindow(JFrame parent, FontificatorProperties fProps)
    {
        super(parent);

        BufferedReader br = null;
        try
        {

            InputStream is = getClass().getClassLoader().getResourceAsStream("help.html");
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder helpBuilder = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                helpBuilder.append(line);
            }
            helpText = helpBuilder.toString();
        }
        catch (Exception e)
        {
            helpText = "Unable to load help file";
            logger.error(helpText, e);
            ChatWindow.popup.handleProblem(helpText);
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (Exception e)
                {
                    logger.error(e.toString(), e);
                }
            }
        }

        ControlWindow.me = this;

        this.manualMessageDialog = new ManualMessageDialog(this);

        setTitle("Fontificator Configuration");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        this.fProps = fProps;

        this.bot = new ChatViewerBot();

        FileFilter cgfFileFilter = new FileNameExtensionFilter("Chat Game Fontificator Configuration (*." + DEFAULT_FILE_EXTENSION + ")", DEFAULT_FILE_EXTENSION.toLowerCase());

        this.opener = new JFileChooser();
        this.opener.setFileFilter(cgfFileFilter);

        this.saver = new JFileChooser();
        this.saver.setFileFilter(cgfFileFilter);
    }

    public void loadLastData(ChatWindow chatWindow)
    {
        this.chatWindow = chatWindow;

        ChatWindow.setupHideOnEscape(this);

        fProps.clear();
        boolean success = fProps.loadLast();

        if (!success)
        {
            fProps.forgetLastConfigFile();
            fProps.loadDefaultValues();
        }

        this.bot.setUsername(fProps.getIrcConfig().getUsername());
    }

    public void build()
    {
        constructAboutPopup();

        this.controlTabs = new ControlTabs(fProps, bot);
        this.controlTabs.build(chatWindow, this);

        this.bot.setChatPanel(chatWindow.getChatPanel());

        setLayout(new GridLayout(1, 1));
        add(controlTabs);

        initMenus();

        // This wasn't built before the config was loaded into the chat control
        // tab, so set it here
        setAlwaysOnTopMenu(fProps.getChatConfig().isAlwaysOnTop());

        setupHelp();

        pack();
        setMinimumSize(getSize());
        setResizable(false);
    }

    private void setupHelp()
    {
        final String helpTitle = "Chat Game Fontificator Help";

        help = new JDialog(this, true);
        help.setTitle(helpTitle);
        help.setSize(640, 480);
        help.setLayout(new GridBagLayout());

        JEditorPane helpPane = new JEditorPane();
        helpPane.setContentType("text/html");
        helpPane.setText(helpText);
        JScrollPane scrollHelp = new JScrollPane(helpPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JButton ok = new JButton("Close");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                help.setVisible(false);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTH;
        help.add(new JLabel(helpTitle), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy = 1;
        help.add(scrollHelp, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 0.0;
        help.add(ok, gbc);

        help.setResizable(false);
    }

    /**
     * Builds the menus from the static arrays
     */
    private void initMenus()
    {
        JMenuBar menuBar = new JMenuBar();

        final String[] mainMenuText = { "File", "Presets", "View", "Message", "Help" };
        final int[] mainMnomonics = { KeyEvent.VK_F, KeyEvent.VK_P, KeyEvent.VK_V, KeyEvent.VK_M, KeyEvent.VK_H };

        JMenu[] menus = new JMenu[mainMenuText.length];

        for (int i = 0; i < mainMenuText.length; i++)
        {
            menus[i] = new JMenu(mainMenuText[i]);
            menus[i].setMnemonic(mainMnomonics[i]);
        }

        /* File Menu Item Text */
        final String strFileOpen = "Open Configuration";
        final String strFileSave = "Save Configuration";
        final String strFileRestore = "Restore Default Configuration";
        final String strFileExit = "Exit";
        final MenuComponent[] fileComponents = new MenuComponent[] { new MenuComponent(strFileOpen, KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK)), new MenuComponent(strFileSave, KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK)), new MenuComponent(strFileRestore, KeyEvent.VK_R, null), new MenuComponent(strFileExit, KeyEvent.VK_X, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK)) };

        /* View Menu Item Text */
        final String strViewTop = "Always On Top";
        final String strViewHide = "Hide Control Window";
        final MenuComponent[] viewComponents = new MenuComponent[] { new MenuComponent(strViewTop, KeyEvent.VK_A, null, true), new MenuComponent(strViewHide, KeyEvent.VK_H, KeyStroke.getKeyStroke(KeyEvent.VK_H, Event.CTRL_MASK)) };

        /* Message Menu Item Text */
        final String strMsgMsg = "Post Manual Message";
        final MenuComponent[] messageComponents = new MenuComponent[] { new MenuComponent(strMsgMsg, KeyEvent.VK_M, KeyStroke.getKeyStroke(KeyEvent.VK_M, Event.CTRL_MASK)) };

        /* Help Menu Item Text */
        final String strHelpHelp = "Help";
        final String strHelpAbout = "About";
        final MenuComponent[] helpComponents = new MenuComponent[] { new MenuComponent(strHelpHelp, KeyEvent.VK_R, null), null, new MenuComponent(strHelpAbout, KeyEvent.VK_A, null) };

        /* All menu components, with a placeholder for the Presets menu */
        final MenuComponent[][] allMenuComponents = new MenuComponent[][] { fileComponents, new MenuComponent[] {}, viewComponents, messageComponents, helpComponents };

        ActionListener mal = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JMenuItem mi = (JMenuItem) e.getSource();
                if (strFileOpen.equals(mi.getText()))
                {
                    open();
                }
                else if (strFileSave.equals(mi.getText()))
                {
                    save();
                }
                else if (strFileRestore.equals(mi.getText()))
                {
                    restoreDefaults();
                }
                else if (strFileExit.equals(mi.getText()))
                {
                    attemptToExit();
                }
                else if (strViewTop.equals(mi.getText()))
                {
                    JCheckBoxMenuItem checkBox = (JCheckBoxMenuItem) e.getSource();
                    ((JFrame) getParent()).setAlwaysOnTop(checkBox.isSelected());
                    controlTabs.setAlwaysOnTopConfig(checkBox.isSelected());
                }
                else if (strViewHide.equals(mi.getText()))
                {
                    setVisible(false);
                }
                else if (strMsgMsg.equals(mi.getText()))
                {
                    manualMessageDialog.setVisible(true);
                }
                else if (strHelpHelp.equals(mi.getText()))
                {
                    help.setVisible(true);
                }
                else if (strHelpAbout.equals(mi.getText()))
                {
                    showAboutPane();
                }
            }
        };

        /* Set all menu items but presets */
        JMenuItem item = null;
        for (int i = 0; i < allMenuComponents.length; i++)
        {
            for (int j = 0; j < allMenuComponents[i].length; j++)
            {
                MenuComponent mc = allMenuComponents[i][j];
                if (mc == null)
                {
                    menus[i].add(new JSeparator());
                }
                else
                {
                    item = mc.checkbox ? new JCheckBoxMenuItem(mc.label) : new JMenuItem(mc.label);
                    item.addActionListener(mal);
                    item.setMnemonic(mc.mnemonic);
                    if (mc.accelerator != null)
                    {
                        item.setAccelerator(mc.accelerator);
                    }
                    menus[i].add(item);
                }
            }
            menuBar.add(menus[i]);
        }

        /* Presets Chrono */
        final String[] strChrono = new String[] { "Chrono Trigger", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "ct.cgf" };
        final String[] strChronoCross = new String[] { "Chrono Cross", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "cc.cgf" };
        /* Presets Dragon Warrior */
        final String[] strDw1 = new String[] { "Dragon Warrior", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "dw1.cgf" };
        final String[] strDw2 = new String[] { "Dragon Warrior II", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "dw2.cgf" };
        final String[] strDw3 = new String[] { "Dragon Warrior III", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "dw3.cgf" };
        final String[] strDw3Gbc = new String[] { "Dragon Warrior III (GBC)", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "dw3gbc.cgf" };
        final String[] strDw4 = new String[] { "Dragon Warrior IV", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "dw4.cgf" };
        /* Presets Earthbound */
        final String[] strEb0 = new String[] { "Earthbound Zero", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb0.cgf" };
        final String[] strEbPlain = new String[] { "Earthbound Plain", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_plain.cgf" };
        final String[] strEbMint = new String[] { "Earthbound Mint", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_mint.cgf" };
        final String[] strEbStrawberry = new String[] { "Earthbound Strawberry", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_strawberry.cgf" };
        final String[] strEbBanana = new String[] { "Earthbound Banana", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_banana.cgf" };
        final String[] strEbPeanut = new String[] { "Earthbound Peanut", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_peanut.cgf" };
        final String[] strEbSaturn = new String[] { "Earthbound Mr. Saturn", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "eb_saturn.cgf" };
        final String[] strM3 = new String[] { "Mother 3", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "m3.cgf" };
        /* Presets Final Fantasy */
        final String[] strFinalFantasy1 = new String[] { "Final Fantasy", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "ff1.cgf" };
        final String[] strFinalFantasy6 = new String[] { "Final Fantasy VI", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "ff6.cgf" };
        /* Presets Mario */
        final String[] strMario1 = new String[] { "Super Mario Bros.", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "smb1.cgf" };
        final String[] strMario1Underworld = new String[] { "Super Mario Bros. Underworld", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "smb1_underworld.cgf" };
        final String[] strMario2 = new String[] { "Super Mario Bros. 2", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "smb2.cgf" };
        final String[] strMario3hud = new String[] { "Super Mario Bros. 3 HUD", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "smb3_hud.cgf" };
        final String[] strMario3letter = new String[] { "Super Mario Bros. 3 Letter", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "smb3_letter.cgf" };
        final String[] strYoshisIsland = new String[] { "Super Mario World 2: Yoshi's Island", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "yi.cgf" };
        /* Presets Metroid */
        final String[] strMetroid = new String[] { "Metroid", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "metroid.cgf" };
        final String[] strMetroidBoss = new String[] { "Metroid Mother Brain", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "metroidboss.cgf" };
        /* Presets Pokemon */
        final String[] strPkmnRb = new String[] { "Pokemon Red/Blue", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "pkmnrb.cgf" };
        final String[] strPkmnFrlg = new String[] { "Pokemon Fire Red/Leaf Green", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "pkmnfrlg.cgf" };
        /* Presets Zelda */
        final String[] strLozBush = new String[] { "The Legend of Zelda Bushes", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "zelda1_bush.cgf" };
        final String[] strLozRock = new String[] { "The Legend of Zelda Moutains", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "zelda1_rock.cgf" };
        final String[] strLozDungeon = new String[] { "The Legend of Zelda Dungeon", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "zelda1_dungeon.cgf" };
        final String[] strZelda2 = new String[] { "Zelda II: The Adventures of Link", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "zelda2.cgf" };
        final String[] strZelda3 = new String[] { "The Legend of Zelda: A Link to the Past", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "zelda3.cgf" };
        /* Ungrouped Presets */
        final String[] strCrystalis = new String[] { "Crystalis", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "crystalis.cgf" };
        final String[] strFreedomPlanet = new String[] { "Freedom Planet", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "freep.cgf" };
        final String[] strGoldenSun = new String[] { "Golden Sun", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "gsun.cgf" };
        final String[] strRiverCityRansom = new String[] { "River City Ransom", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "rcr.cgf" };
        final String[] strSecretOfEvermore = new String[] { "Secret of Evermore", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "soe.cgf" };
        final String[] strTalesOfSymphonia = new String[] { "Tales of Symphonia", ConfigFont.INTERNAL_FILE_PREFIX + PRESET_DIRECTORY + "tos.cgf" };

        // @formatter:off
        final String[][] allPresets = new String[][]
        {
            strChrono, strChronoCross, 
            strDw1, strDw2, strDw3, strDw3Gbc, strDw4, 
            strEb0, strEbPlain, strEbMint, strEbStrawberry, strEbBanana, strEbPeanut, strEbSaturn, strM3,
            strFinalFantasy1, strFinalFantasy6,  
            strMario1, strMario1Underworld, strMario2, strMario3hud, strMario3letter, strYoshisIsland, 
            strMetroid, strMetroidBoss, 
            strPkmnRb, strPkmnFrlg, 
            strLozBush, strLozRock, strLozDungeon, strZelda2, strZelda3, 
            strCrystalis, strFreedomPlanet, strGoldenSun, strRiverCityRansom, strSecretOfEvermore, strTalesOfSymphonia
        };
        // @formatter:on

        ActionListener presetListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String sourceText = ((JMenuItem) e.getSource()).getText();
                for (int i = 0; i < allPresets.length; i++)
                {
                    if (allPresets[i][0].equals(sourceText))
                    {
                        loadPreset(allPresets[i][0], allPresets[i][1]);
                        break;
                    }
                }
            }
        };

        // Put all the presets into this map to convert them into the submenu items
        final Map<String, String[]> presetMapSubmenuToItem = new LinkedHashMap<String, String[]>();
        presetMapSubmenuToItem.put("Chrono", new String[] { strChrono[0], strChronoCross[0] });
        presetMapSubmenuToItem.put("Dragon Warrior", new String[] { strDw1[0], strDw2[0], strDw3[0], strDw3Gbc[0], strDw4[0] });
        presetMapSubmenuToItem.put("Earthbound", new String[] { strEb0[0], strEbPlain[0], strEbMint[0], strEbStrawberry[0], strEbBanana[0], strEbPeanut[0], strEbSaturn[0], strM3[0] });
        presetMapSubmenuToItem.put("Final Fantasy", new String[] { strFinalFantasy1[0], strFinalFantasy6[0] });
        presetMapSubmenuToItem.put("Mario", new String[] { strMario1[0], strMario2[0], strMario3hud[0], strMario3letter[0], strYoshisIsland[0] });
        presetMapSubmenuToItem.put("Metroid", new String[] { strMetroid[0], strMetroidBoss[0] });
        presetMapSubmenuToItem.put("Pokemon", new String[] { strPkmnRb[0], strPkmnFrlg[0] });
        presetMapSubmenuToItem.put("Zelda", new String[] { strLozBush[0], strLozRock[0], strLozDungeon[0], strZelda2[0], strZelda3[0] });
        presetMapSubmenuToItem.put(null, new String[] { strCrystalis[0], strFreedomPlanet[0], strGoldenSun[0], strRiverCityRansom[0], strSecretOfEvermore[0], strTalesOfSymphonia[0] });

        for (String submenuKey : presetMapSubmenuToItem.keySet())
        {
            String[] submenuItems = presetMapSubmenuToItem.get(submenuKey);
            if (submenuKey != null)
            {
                JMenu submenu = new JMenu(submenuKey);
                for (String itemStr : submenuItems)
                {
                    JMenuItem submenuItem = new JMenuItem(itemStr);
                    submenuItem.addActionListener(presetListener);
                    submenu.add(submenuItem);
                }
                menus[1].add(submenu);
            }
            else
            {
                for (String submenuRootItemStr : submenuItems)
                {
                    JMenuItem submenuRootItem = new JMenuItem(submenuRootItemStr);
                    submenuRootItem.addActionListener(presetListener);
                    menus[1].add(submenuRootItem);
                }
            }
        }

        for (int i = 0; i < menus.length; i++)
        {
            menuBar.add(menus[i]);
        }

        setJMenuBar(menuBar); // add the whole menu bar
    }

    private void restoreDefaults()
    {
        boolean okayToProceed = fProps.checkForUnsavedProps(this, this);

        if (okayToProceed)
        {
            int result = JOptionPane.showConfirmDialog(this, "Reset to default configuration?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION)
            {
                fProps.loadDefaultValues();
                controlTabs.refreshUiFromConfig(fProps);
                chatWindow.getChatPanel().repaint();
            }
        }
    }

    private void open()
    {
        boolean okayToProceed = fProps.checkForUnsavedProps(this, this);

        if (okayToProceed)
        {
            int result = opener.showOpenDialog(me);
            if (result == JFileChooser.APPROVE_OPTION)
            {
                try
                {
                    boolean success = fProps.loadFile(opener.getSelectedFile());
                    if (!success)
                    {
                        throw new Exception("Configuration file open error");
                    }
                    controlTabs.refreshUiFromConfig(fProps);
                    chatWindow.getChatPanel().repaint();
                }
                catch (Exception ex)
                {
                    logger.error("Configuration file open error", ex);
                }
            }
        }
    }

    private void loadPreset(String presetName, String presetFilename)
    {
        boolean okayToProceed = fProps.checkForUnsavedProps(this, this);
        if (okayToProceed)
        {
            try
            {
                boolean success = fProps.loadFile(presetFilename);
                if (!success)
                {
                    logger.error("Unsuccessful call to FontificatorProperties.loadFile(String)");
                    throw new Exception();
                }
            }
            catch (Exception ex)
            {
                logger.error(ex.toString(), ex);
                ChatWindow.popup.handleProblem("Unable to load preset " + presetName + " (" + presetFilename + ")");
            }
            controlTabs.refreshUiFromConfig(fProps);
            chatWindow.getChatPanel().repaint();
        }
    }

    /**
     * @return saved
     */
    public boolean save()
    {
        final boolean configReadyToSave = controlTabs.refreshConfigFromUi();
        if (configReadyToSave)
        {
            int overwrite = JOptionPane.YES_OPTION;
            // Do while overwrite is no, so it loops back around to try again if someone says they don't want to
            // overwrite an existing file, but if they select cancel it just breaks out of the loop
            do
            {
                int result = saver.showSaveDialog(me);

                // Default to yes, so it writes even if there's no existing file
                overwrite = JOptionPane.YES_OPTION;

                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File saveFile = saver.getSelectedFile();
                    String[] exts = ((FileNameExtensionFilter) (saver.getFileFilter())).getExtensions();
                    boolean endsInExt = false;
                    for (String ext : exts)
                    {
                        if (saveFile.getName().toLowerCase().endsWith(ext.toLowerCase()))
                        {
                            endsInExt = true;
                            break;
                        }
                    }
                    if (!endsInExt)
                    {
                        saveFile = new File(saveFile.getPath() + "." + DEFAULT_FILE_EXTENSION);
                    }

                    if (saveFile.exists())
                    {
                        overwrite = JOptionPane.showConfirmDialog(me, "File " + saveFile.getName() + " already exists. Overwrite?", "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    }

                    if (overwrite == JOptionPane.YES_OPTION)
                    {
                        try
                        {
                            fProps.saveFile(saveFile);
                            return true;
                        }
                        catch (Exception ex)
                        {
                            logger.error("Configuration file save error", ex);
                        }
                    }
                }
            } while (overwrite == JOptionPane.NO_OPTION);
        }
        return false;
    }

    public void setAlwaysOnTopMenu(boolean alwaysOnTop)
    {
        ((JCheckBoxMenuItem) (getJMenuBar().getMenu(2).getItem(0))).setSelected(alwaysOnTop);
    }

    public void clearUsernameCases()
    {
        bot.clearUsernameCases();
    }

    public void addManualMessage(String username, String message)
    {
        bot.sendMessageToChat(MessageType.MANUAL, username, message);
    }

    public void disconnect()
    {
        bot.disconnect();
    }

    public void attemptToExit()
    {
        attemptToExit(this);
    }

    /**
     * Any program exit should call this method to do so
     */
    public void attemptToExit(Component parent)
    {
        boolean okayToProceed = fProps.checkForUnsavedProps(this, parent);
        if (okayToProceed)
        {
            disconnect();
            System.exit(0);
        }
    }

    /**
     * Construct the popup dialog containing the About message
     */
    private void constructAboutPopup()
    {
        aboutPane = new JEditorPane("text/html", ABOUT_CONTENTS);
        aboutPane.addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (EventType.ACTIVATED.equals(e.getEventType()))
                {
                    if (Desktop.isDesktopSupported())
                    {
                        try
                        {
                            Desktop.getDesktop().browse(URI.create("https://" + e.getDescription()));
                        }
                        catch (IOException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        aboutPane.setEditable(false);
    }

    private void showAboutPane()
    {
        JOptionPane.showMessageDialog(this, aboutPane, "About", JOptionPane.PLAIN_MESSAGE);
    }
}
