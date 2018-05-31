package com.github.maktoff.assistant.view;

import com.github.maktoff.assistant.model.content.ContentType;
import com.github.maktoff.assistant.model.content.discussion.Discussions;
import com.github.maktoff.assistant.model.content.snippet.SnippetListener;
import com.github.maktoff.assistant.model.content.snippet.Snippets;
import com.github.maktoff.assistant.presentationModel.ContentHolder;
import com.github.maktoff.assistant.presentationModel.LanguageFacade;
import com.github.maktoff.assistant.presentationModel.Logger;
import com.github.maktoff.assistant.presentationModel.Settings;
import com.github.maktoff.assistant.view.controls.ActionButton;
import com.github.maktoff.assistant.view.controls.SearchBar;
import com.github.maktoff.assistant.view.controls.SnippetPanel;
import com.github.maktoff.assistant.view.helpers.Icons;
import com.github.maktoff.assistant.view.helpers.UITypes;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class AssistantToolWindow extends JPanel {
    private final PreviousButton prevButton = new PreviousButton();
    private final NextButton nextButton = new NextButton();
    private final FavouriteButton favouriteButton = new FavouriteButton();
    private final int snippetTabIndex = 0;
    private final int discussionTabIndex = 1;

    private JPanel toolWindowContent;
    private SearchBar searchBar;
    private JComboBox<String> languageBox;
    private Wrapper toolPanel;
    private JLabel amountLabel;
    private JTabbedPane tabbedPane;
    private SnippetPanel snippetPanel;
    private JPanel discussionWebViewPanel;
    private JLabel noSnippetFoundLabel;
    private JLabel noDiscussionFoundLabel;
    private JSeparator separator;
    private JProgressBar progressBar;
    private JLabel discussionLinkLabel;
    private WebView webView;
    private ContentHolder contentHolder;

    public AssistantToolWindow(@NotNull Project project) {
        Platform.setImplicitExit(false);

        Settings settings = new Settings();
        Logger.INSTANCE.load(settings.getId());

        contentHolder = new ContentHolder(project);

        for (String item : LanguageFacade.INSTANCE.getAllLanguages()) {
            languageBox.addItem(item);
        }

        resetLanguage();
        addFileEditorManagerListener(project);

        searchBar.setSearchAction(this::search);
        searchBar.updateLanguage(getCurrentLanguage());
        searchBar.grabFocus();

        createToolbar();

        separator.setForeground(UIUtil.isUnderDarcula() ? Gray._50 : JBColor.LIGHT_GRAY);

        snippetPanel.setVisible(false);
        progressBar.setVisible(false);
        createDiscussionsPanel();

        setTabSettings(snippetTabIndex, "Snippets", Icons.INSTANCE.getCode());
        setTabSettings(discussionTabIndex, "Discussions", Icons.INSTANCE.getStackOverflow());

        setListeners();
    }

    public JPanel getToolWindowContent() {
        return toolWindowContent;
    }

    private String getCurrentLanguage() {
        return Objects.requireNonNull(languageBox.getSelectedItem()).toString();
    }

    private void resetLanguage() {
        String language = contentHolder.resetLanguage();

        if (!language.isEmpty()) {
            int index = ((DefaultComboBoxModel) languageBox.getModel()).getIndexOf(language);

            if (index == -1) {
                languageBox.addItem(language);
            }

            languageBox.setSelectedItem(language);
        }
    }

    private void addFileEditorManagerListener(@NotNull Project project) {
        MessageBus messageBus = project.getMessageBus();

        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                resetLanguage();
            }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                resetLanguage();
            }
        });
    }

    private void setListeners() {
        languageBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                searchBar.updateLanguage(getCurrentLanguage());
            }
        });

        amountLabel.addPropertyChangeListener("text", e -> {
            if (e.getNewValue().equals("0 / 0")) {
                amountLabel.setText("");
            }
        });

        tabbedPane.addChangeListener(e -> {
            switch (tabbedPane.getSelectedIndex()) {
                case snippetTabIndex:
                    contentHolder.setContentType(ContentType.SNIPPET);
                    break;
                case discussionTabIndex:
                    contentHolder.setContentType(ContentType.DISCUSSION);
                    break;
            }

            activateView();
        });

        contentHolder.getSnippets().addListener(new SnippetListener() {
            @Override
            public void actionPerformed() {
                progressBar.setMaximum(getMaxValue());
                progressBar.setValue(getCurrentValue());

                if (getMaxValue() == getCurrentValue()) {
                    progressBar.setIndeterminate(true);
                }
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() == KeyEvent.VK_MINUS && e.getID() == KeyEvent.KEY_PRESSED &&
                    (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 && prevButton.isActive()) {
                prevButton.actionPerformed(null);
            } else if (e.getKeyCode() == KeyEvent.VK_EQUALS && e.getID() == KeyEvent.KEY_PRESSED &&
                    (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 && nextButton.isActive()) {
                nextButton.actionPerformed(null);
            }

            return false;
        });
    }

    private void activateView() {
        amountLabel.setText(contentHolder.getCurrentNumber() + " / " + contentHolder.getSize());

        if (contentHolder.getSize() > 0) {
            activateButtons(true);
        } else {
            activateButtons(false);
        }
    }

    private void createToolbar() {
        final DefaultActionGroup group = new DefaultActionGroup();

        group.add(prevButton);
        group.add(nextButton);
        group.addSeparator();
        group.add(favouriteButton);

        activateButtons(false);

        final ActionToolbarImpl toolbar = (ActionToolbarImpl) ActionManager.getInstance().createActionToolbar("Assistant toolbar", group, true);
        toolbar.setReservePlaceAutoPopupIcon(false);
        toolbar.setAddSeparatorFirst(true);

        toolPanel.setLayout(new BorderLayout());
        toolPanel.add(toolbar);
    }

    private void createDiscussionsPanel() {
        final JFXPanel panel = new JFXPanel();

        Platform.runLater(() -> {
            webView = new WebView();
            String userAgent = "Mozilla/5.0 (Linux; U; Android 6.0.1; en) AppleWebKit/534.31+ (KHTML, like Gecko) Version/4.0 Mobile Safari/534.31";

            webView.getEngine().setUserAgent(userAgent);
            panel.setScene(new Scene(webView));
        });

        discussionWebViewPanel.add(panel, BorderLayout.CENTER);
        discussionWebViewPanel.setVisible(false);
    }

    private void setTabSettings(int index, String title, Icon icon) {
        JLabel label = new JLabel(title);

        label.setIcon(icon);
        label.setBorder(JBUI.Borders.empty(1));
        tabbedPane.setTabComponentAt(index, label);

        UIUtil.addInsets(label, UIUtil.PANEL_SMALL_INSETS);
    }

    private void activateButtons(boolean value) {
        prevButton.setActive(value);
        nextButton.setActive(value);
        favouriteButton.setActive(value);
    }

    private void search() {
        if (!searchBar.getText().isEmpty()) {
            Logger.INSTANCE.searchCalled(searchBar.getText(), getCurrentLanguage(),
                    UITypes.TOOL_WINDOW.getValue(), contentHolder.getContentType().getValue());

            activateButtons(false);
            contentHolder.reset(searchBar.getText(), getCurrentLanguage(), this::loadSnippets, this::loadDiscussions);

            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            progressBar.setVisible(true);
            noSnippetFoundLabel.setVisible(false);
            snippetPanel.repaint();
            snippetPanel.setVisible(false);
        } else {
            Messages.showMessageDialog("The input string is empty.", "Warning", Messages.getWarningIcon());
        }
    }

    private void loadSnippets() {
        Snippets snippets = contentHolder.getSnippets();

        if (snippets.size() == 0 || snippets.getCurrentNumber() == 0) {
            amountLabel.setText("");
            progressBar.setVisible(false);
            noSnippetFoundLabel.setVisible(true);
            snippetPanel.repaint();
            snippetPanel.setVisible(false);

            return;
        }

        snippetPanel.reset(contentHolder, getCurrentLanguage());
        snippetPanel.repaint();

        activateView();

        progressBar.setVisible(false);
        noSnippetFoundLabel.setVisible(false);
        snippetPanel.setVisible(true);
    }

    private void loadDiscussions() {
        Discussions discussions = contentHolder.getDiscussions();

        if (discussions.size() == 0 || discussions.getCurrentNumber() == 0) {
            amountLabel.setText("");
            noDiscussionFoundLabel.setVisible(true);
            discussionLinkLabel.setVisible(false);
            discussionWebViewPanel.setVisible(false);

            return;
        }

        String link = discussions.getContent();

        discussionLinkLabel.setText("Source: " + link.substring(0, link.lastIndexOf("/")));

        if (!link.isEmpty()) {
            Platform.runLater(() -> {
                WebEngine webEngine = webView.getEngine();
                webEngine.load(link);
            });
        }

        activateView();

        noDiscussionFoundLabel.setVisible(false);
        discussionLinkLabel.setVisible(true);
        discussionWebViewPanel.setVisible(true);
    }

    private void logAndCall() {
        Logger.INSTANCE.clicked(contentHolder.getCurrentNumber(), contentHolder.getContentType().getValue());

        switch (contentHolder.getContentType()) {
            case SNIPPET:
                loadSnippets();
                break;
            case DISCUSSION:
                loadDiscussions();
                break;
        }
    }

    private final class PreviousButton extends ActionButton {
        PreviousButton() {
            super("Previous item (Ctrl+Minus)", Icons.INSTANCE.getPrevious());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            contentHolder.previousElement();
            logAndCall();
        }
    }

    private final class NextButton extends ActionButton {
        NextButton() {
            super("Next item (Ctrl+Plus)", Icons.INSTANCE.getNext());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            contentHolder.nextElement();
            logAndCall();
        }
    }

    private final class FavouriteButton extends ActionButton {
        FavouriteButton() {
            super("Mark as helpful", Icons.INSTANCE.getStar());
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Logger.INSTANCE.favouriteClicked(contentHolder.getCurrentNumber(), contentHolder.getContentType().getValue());
        }
    }
}