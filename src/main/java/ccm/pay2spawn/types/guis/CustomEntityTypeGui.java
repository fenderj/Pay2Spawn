/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn.types.guis;

import ccm.pay2spawn.configurator.Configurator;
import ccm.pay2spawn.network.NbtRequestPacket;
import ccm.pay2spawn.network.TestPacket;
import ccm.pay2spawn.util.IIHasCallback;
import ccm.pay2spawn.util.JsonNBTHelper;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class CustomEntityTypeGui extends HelperGuiBase implements IIHasCallback
{
    public JButton     importItemYouAreButton;
    public JScrollPane scrollPane;
    public JTextPane   jsonPane;
    public JButton     parseFromJsonButton;
    public JButton     saveButton;
    public JButton     updateJsonButton;
    public JButton     testButton;
    public JPanel      panel1;
    public CustomEntityTypeGui instance = this;

    public CustomEntityTypeGui(int rewardID, String name, JsonObject inputData, HashMap<String, String> typeMap)
    {
        super(rewardID, name, inputData, typeMap);

        makeAndOpen();
    }

    @Override
    public void readJson()
    {
        jsonPane.setText(JsonNBTHelper.GSON.toJson(data));
    }

    @Override
    public void updateJson()
    {
        jsonPane.setText(JsonNBTHelper.GSON.toJson(data));
    }

    @Override
    public void setupListeners()
    {
        testButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                TestPacket.sendToServer(name, data);
            }
        });
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
                Configurator.instance.callback(rewardID, name, data);
                dialog.dispose();
            }
        });
        parseFromJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    data = JsonNBTHelper.PARSER.parse(jsonPane.getText()).getAsJsonObject();
                    readJson();
                    jsonPane.setForeground(Color.black);
                }
                catch (Exception e1)
                {
                    jsonPane.setForeground(Color.red);
                    e1.printStackTrace();
                }
            }
        });
        updateJsonButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateJson();
            }
        });
        importItemYouAreButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                NbtRequestPacket.requestEntity(instance);
            }
        });
    }

    @Override
    public void callback(Object... data)
    {
        this.data = JsonNBTHelper.PARSER.parse((String) data[0]).getAsJsonObject();
        updateJson();
    }

    @Override
    public JPanel getPanel()
    {
        return panel1;
    }

    {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        importItemYouAreButton = new JButton();
        importItemYouAreButton.setText("Import the next mob you right click ingame. Don't close this window untill you do!");
        importItemYouAreButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(importItemYouAreButton, gbc);
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("For mobs riding other mobs: You must right click the top one.");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel2.add(label1, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel3, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Json:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label2, gbc);
        scrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(scrollPane, gbc);
        jsonPane = new JTextPane();
        jsonPane.setEnabled(true);
        jsonPane.setText("");
        jsonPane.setToolTipText("Make sure you hit \"Parse from JSON\" after editing this!");
        scrollPane.setViewportView(jsonPane);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel4, gbc);
        parseFromJsonButton = new JButton();
        parseFromJsonButton.setText("Parse from Json");
        parseFromJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(parseFromJsonButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(saveButton, gbc);
        updateJsonButton = new JButton();
        updateJsonButton.setText("Update Json");
        updateJsonButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(updateJsonButton, gbc);
        testButton = new JButton();
        testButton.setText("Test");
        testButton.setToolTipText("Push the button!");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(testButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    { return panel1; }
}
