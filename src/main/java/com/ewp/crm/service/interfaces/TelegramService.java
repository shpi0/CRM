package com.ewp.crm.service.interfaces;

import org.drinkless.tdlib.TdApi;

public interface TelegramService {

    void sendAuthPhone(String phone);

    void sentAuthCode(String smsCode);

    boolean isAuthenticated();

    boolean isTdlibInstalled();

    TdApi.Messages getChatMessages(long chatId, int limit);

    TdApi.Messages getUnreadMessagesFromChat(long chatId, int limit);

    void sendChatMessage(long chatId, String text);

    TdApi.Chat getChat(long chatId);

    void closeChat(long chatId);

    public TdApi.User getMe();

    public TdApi.User getUserById(int userId);

    public TdApi.UserProfilePhotos getUserPhotos(int userId);

    void logout();
}
