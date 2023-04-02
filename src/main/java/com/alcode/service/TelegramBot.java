package com.alcode.service;

import com.alcode.config.BotConfig;
import com.alcode.user.Role;
import com.alcode.user.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UsersService usersService;

    public TelegramBot(BotConfig config, UsersService usersService) {
        this.config = config;
        this.usersService = usersService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Boshlash"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getChat().getType().equals("supergroup")) {
                // DO NOTHING CHANNEL CHAT ID IS -1001764816733
                return;
            } else {
                Role role = usersService.getRoleByChatId(chatId);

                if (update.hasMessage() && update.getMessage().hasText()) {
                    String messageText = update.getMessage().getText();

                    if (messageText.startsWith("/")) {
                        if (messageText.startsWith("/login ")) {
                            String password = messageText.substring(7);

                            if (password.equals("Xp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6w9z$C&F)J@NcRfUjXn2r4u7x!A%D*G-Ka")) {
                                usersService.changeRole(chatId, Role.ROLE_ADMIN);
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            return;
                        }

                        switch (messageText) {
                            case "/start" -> {
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            case "/help" -> {
                                helpCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                                return;
                            }
                            default -> {
                                sendMessage(chatId, "Sorry, command was not recognized");
                                return;
                            }
                        }
                    }

                    if (role.equals(Role.ROLE_ADMIN)) {

                    }
                    else if (role.equals(Role.ROLE_USER)) {}
                }
                if (update.hasMessage() && update.getMessage().hasPhoto()) {

                }
            }

        }
    }

    private void startCommandReceived(long chatId, String firstName, String lastName) {
        Role role = usersService.createUser(chatId, firstName, lastName).getRole();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableHtml(true);

        if (role.equals(Role.ROLE_USER)) {
            message.setText("Welcome User, What's up?");
        } else if (role.equals(Role.ROLE_ADMIN)) {
            message.setText("Welcome Admin, What's up?");
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error in startCommandReceived()");
        }
    }

    private void helpCommandReceived(long chatId, String firstName) {
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }
}