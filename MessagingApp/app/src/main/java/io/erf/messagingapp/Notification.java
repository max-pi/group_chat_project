package io.erf.messagingapp;

import java.io.Serializable;

/**
 * Created by nathan on 2017-03-27.
 */

public class Notification implements Serializable {
    public String message;
    public int GroupId;
    public String GroupName;
    public String SenderName;
}
