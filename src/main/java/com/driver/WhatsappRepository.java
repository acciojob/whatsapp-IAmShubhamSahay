package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        userMobile.add(mobile);
        return "SUCESS";
    }

    public Group createGroup(List<User> users){
        if (users.size() < 2){
            throw new IllegalArgumentException("A group must have at least 2 users");
        }
        User admin = users.get(0);
        Group group;
        if (users.size() == 2){
            group = new Group(users.get(1).getName(),2);

        }else{
            this.customGroupCount++;
            group = new Group("Group " + this.customGroupCount,users.size());
        }
        groupUserMap.put(group,users);
        adminMap.put(group,admin);
        return group;

    }

    public int createMessage(String content){
        this.messageId++;
        Message message = new Message(this.messageId,content,new Date());
        return message.getId();
    }

    public int sendMessage(Message message,User sender,Group group) throws Exception{
        if(!groupUserMap.containsKey(group)){
            throw  new Exception("Group does not exist");
        }
        List<User> users = groupUserMap.get(group);
        if(!users.contains(sender)){
            throw new Exception("You are not allowed to send message");
        }
        groupMessageMap.putIfAbsent(group,new ArrayList<>());
        groupMessageMap.get(group).add(message);
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }
        if (!adminMap.get(group).equals(approver)) {
            throw new Exception("Approver does not have rights");
        }
        List<User> users = groupUserMap.get(group);
        if (!users.contains(user)) {
            throw new Exception("User is not a participant");
        }
        adminMap.put(group, user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        Group groupToRemoveFrom = null;
        for (Map.Entry<Group, List<User>> entry : groupUserMap.entrySet()) {
            if (entry.getValue().contains(user)) {
                groupToRemoveFrom = entry.getKey();
                break;
            }
        }
        if (groupToRemoveFrom == null) {
            throw new Exception("User not found");
        }
        if (adminMap.get(groupToRemoveFrom).equals(user)) {
            throw new Exception("Cannot remove admin");
        }
        groupUserMap.get(groupToRemoveFrom).remove(user);

        List<Message> messagesToRemove = new ArrayList<>();
        for (Map.Entry<Message, User> entry : senderMap.entrySet()) {
            if (entry.getValue().equals(user)) {
                messagesToRemove.add(entry.getKey());
            }
        }
        for (Message message : messagesToRemove) {
            senderMap.remove(message);
            groupMessageMap.get(groupToRemoveFrom).remove(message);
        }
        return groupUserMap.get(groupToRemoveFrom).size() +
                groupMessageMap.get(groupToRemoveFrom).size() +
                senderMap.size();
    }

    public String findMessage(Date start, Date end, int K) throws Exception {
        List<Message> messages = new ArrayList<>();
        for (List<Message> messageList : groupMessageMap.values()) {
            for (Message message : messageList) {
                if (message.getTimestamp().after(start) && message.getTimestamp().before(end)) {
                    messages.add(message);
                }
            }
        }
        messages.sort(Comparator.comparing(Message::getTimestamp).reversed());
        if (messages.size() < K) {
            throw new Exception("K is greater than the number of messages");
        }
        return messages.get(K - 1).getContent();
    }




}
