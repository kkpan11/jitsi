/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.util.plugin.wizard;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.codec.*;
import org.jitsi.service.resources.*;


/**
 * The encodings configuration panel (used in the account configuration wizards)
 * 
 * @author Boris Grozev
 */
public class EncodingsPanel extends TransparentPanel
{
    /**
     * The <tt>Logger</tt> used by the <tt>EncodingsPanel</tt> class for
     * logging output.
     */
    private static final Logger logger = Logger.getLogger(EncodingsPanel.class);

    /**
     * The <tt>ResourceManagementService</tt> used by this class
     */
    private static ResourceManagementService resourceService
            = UtilActivator.getResources();

    /**
     * The "override global settings" checkbox.
     */
    private final JCheckBox overrideCheckBox;

    /**
     * The <tt>MediaConfiguration</tt> instance we'll use to obtain most of the
     * <tt>Component</tt>s for the panel
     */
    private final MediaConfigurationService mediaConfiguration;

    /**
     * A panel to hold the audio encodings table
     */
    private JPanel audioPanel;

    /**
     * The audio encodings table (and "up"/"down" buttons)
     */
    private Component audioControls;

    /**
     * A panel to hold the video encodings table
     */
    private JPanel videoPanel;

    /**
     * The video encodings table (and "up"/"down" buttons)
     */
    private Component videoControls;

    /**
     * Holds the properties we need to get/set for the encoding preferences
     */
    private Map<String, String> encodingProperties
            = new HashMap<String, String>();

    /**
     * An <tt>EncodingConfiguration</tt> we'll be using to manage preferences
     * for us
     */
    private EncodingConfiguration encodingConfiguration;

    /**
     * The "reset" button
     */
    private JButton resetButton = new JButton(resourceService.getI18NString(
            "plugin.jabberaccregwizz.RESET"));

    /**
     * Builds an object, loads the tables with the global configuration..
     */
    public EncodingsPanel()
    {
        super(new BorderLayout());
                
        overrideCheckBox = new SIPCommCheckBox(resourceService.
            getI18NString("plugin.jabberaccregwizz.OVERRIDE_ENCODINGS"),
                false);
    
        mediaConfiguration 
                = UtilActivator.getMediaConfiguration();

        //by default (on account creation) use an <tt>EncodingConfiguration</tt>
        //loaded with the global preferences. But make a new instance, because
        //we do not want to change the current one
        encodingConfiguration = mediaConfiguration.getMediaService()
                .createEmptyEncodingConfiguration();
        encodingConfiguration.loadEncodingConfiguration(mediaConfiguration
                .getMediaService().getCurrentEncodingConfiguration());

        audioControls = mediaConfiguration.
                createEncodingControls(MediaType.AUDIO, encodingConfiguration);
        videoControls = mediaConfiguration.
                createEncodingControls(MediaType.VIDEO, encodingConfiguration);

        JPanel mainPanel = new TransparentPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel, BorderLayout.NORTH);

        JPanel checkBoxPanel
            = new TransparentPanel(new BorderLayout());
        checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        checkBoxPanel.add(overrideCheckBox,BorderLayout.WEST);
        resetButton.setToolTipText(resourceService.getI18NString(
                "plugin.jabberaccregwizz.RESET_DESCRIPTION"));
        checkBoxPanel.add(resetButton,BorderLayout.EAST);
        resetButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                encodingConfiguration.loadEncodingConfiguration(
                        mediaConfiguration.getMediaService()
                                .getCurrentEncodingConfiguration());
                encodingConfiguration.storeProperties(encodingProperties,
                        ProtocolProviderFactory.ENCODING_PROP_PREFIX+".");
                resetTables();
            }
        });

        audioPanel = new TransparentPanel(new BorderLayout(10, 10));
        audioPanel.setBorder(BorderFactory.createTitledBorder(
               resourceService.getI18NString("plugin.jabberaccregwizz.AUDIO")));
        audioPanel.add(audioControls);
        
        videoPanel = new TransparentPanel(new BorderLayout(10, 10));
        videoPanel.setBorder(BorderFactory.createTitledBorder(
               resourceService.getI18NString("plugin.jabberaccregwizz.VIDEO")));
        videoPanel.add(videoControls);
              
        mainPanel.add(checkBoxPanel);
        mainPanel.add(audioPanel);
        mainPanel.add(videoPanel);
    }

    /**
     * Saves the settings we hold in <tt>registration</tt>
     * @param registration the <tt>EncodingsRegistration</tt> to use
     */
    public void commitPanel(EncodingsRegistration registration)
    {
        registration.setOverrideEncodings(overrideCheckBox.isSelected());
        
        encodingConfiguration.storeProperties(encodingProperties,
                ProtocolProviderFactory.ENCODING_PROP_PREFIX+".");

        registration.setEncodingProperties(encodingProperties);
    }

    /**
     * Checks the given <tt>accountProperties</tt> for encoding configuration
     * and loads it.
     * @param accountProperties the properties to use.
     */
    public void loadAccount(Map<String, String> accountProperties)
    {
        String overrideEncodings = accountProperties.get(
                ProtocolProviderFactory.OVERRIDE_ENCODINGS);
        boolean isOverrideEncodings = Boolean.parseBoolean(overrideEncodings);
        overrideCheckBox.setSelected(isOverrideEncodings);

        encodingConfiguration = mediaConfiguration.getMediaService()
                .createEmptyEncodingConfiguration();
        encodingConfiguration.loadProperties(accountProperties,
                ProtocolProviderFactory.ENCODING_PROP_PREFIX);
        encodingConfiguration.storeProperties(encodingProperties,
                ProtocolProviderFactory.ENCODING_PROP_PREFIX+".");

        resetTables();

    }

    /**
     * Recreates the audio and video controls. Necessary when
     * our encodingConfiguration reference has changed.
     */
    private void resetTables()
    {
        audioPanel.remove(audioControls);
        videoPanel.remove(videoControls);
        audioControls = mediaConfiguration.
                createEncodingControls(MediaType.AUDIO, encodingConfiguration);
        videoControls = mediaConfiguration.
                createEncodingControls(MediaType.VIDEO, encodingConfiguration);

        audioPanel.add(audioControls);
        videoPanel.add(videoControls);
    }
}