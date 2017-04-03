package main

import (
    "fmt"
    "net/http"
    "log"
    "strconv"
    "encoding/json"
    "strings"
)

var SUCCESS_MESSAGE = "[{\"Success\": true}]";

// handlers for the api calls

func handler(w http.ResponseWriter, r *http.Request) {
}

func handler_status(w http.ResponseWriter, r *http.Request) {
    //log.Println("status")
    //TODO: check for notifications here?

    fmt.Fprintf(w, SUCCESS_MESSAGE)
}

func handler_app_link(w http.ResponseWriter, r *http.Request) {
    log.Println("app")
    // TODO: redirect to the app store link
    fmt.Fprintf(w, "app store link:")

}

func handler_group_all(w http.ResponseWriter, r *http.Request) {
    log.Println("all groups")

    all_groups := get_group_all()

    text, err := json.Marshal(all_groups)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_group_one(w http.ResponseWriter, r *http.Request) {
    log.Println("one group")

    if r.Method != "GET" {
      // only allow GET requests
      return
    }

    params := strings.Split(r.URL.Path,"/")

    group_id := params[len(params)-1] // gets trailing part
    group_id_int, err := strconv.Atoi(group_id)

    group := get_group_with_id(group_id_int)
    //members := get_group_members(group_id_int)

    text, err := json.Marshal(group)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    // show the group info
    fmt.Fprintf(w, "[" + string(text) + "]")
}

func handler_group_new(w http.ResponseWriter, r *http.Request) {
    log.Println("new group")

    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    decoder := json.NewDecoder(r.Body)

    datas := []struct {
      GroupName string
    }{}

    err := decoder.Decode(&datas)

    if(err != nil) {
      fmt.Fprintf(w, string(err.Error()))
    }

    data := datas[0]

    group := create_group(data.GroupName)

    text, err := json.Marshal([]Group{group})
    if (err != nil) {
      // rip
      return
    }
    // prints group obj
    fmt.Fprintf(w, string(text))
}

func handler_group_delete(w http.ResponseWriter, r *http.Request) {
    log.Println("delete group")

    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    group_id := r.PostFormValue("group_id")
    group_id_int, err := strconv.Atoi(group_id)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    delete_group(group_id_int)
}

func handler_group_join(w http.ResponseWriter, r *http.Request) {
    log.Println("join group")


    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    decoder := json.NewDecoder(r.Body)

    datas := []struct {
      GroupId int
      UserId int
    }{}

    err := decoder.Decode(&datas)

    if(err != nil) {
      fmt.Fprintf(w, string(err.Error()))
    }

    data := datas[0]

    join_group(data.GroupId, data.UserId)
    fmt.Fprintf(w, "[{\"GroupId\": %d }]", data.GroupId)
}

func handler_group_kick(w http.ResponseWriter, r *http.Request) {
    log.Println("kick")

    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    decoder := json.NewDecoder(r.Body)

    datas := []struct {
      GroupId int
      UserId int
    }{}

    err := decoder.Decode(&datas)

    if(err != nil) {
      fmt.Fprintf(w, string(err.Error()))
    }

    data := datas[0]

    kick_user(data.GroupId, data.UserId)
    fmt.Fprintf(w, "[{\"GroupId\": %d }]", data.GroupId)
}


func handler_user_new_or_rename(w http.ResponseWriter, r *http.Request) {
    log.Println("new or rename user")

    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    decoder := json.NewDecoder(r.Body)
    var users []User
    err := decoder.Decode(&users)

    if(err != nil) {
      fmt.Fprintf(w, string(err.Error()))
    }

    user := users[0]

    log.Println(user.UserId)

    if(user.UserId > -1) {
      // rename a user

      user := rename_user(user.UserId, user.Name)

      text, err := json.Marshal([]User{user})
      if (err != nil) {
        // rip
        return
      }
      // prints id of the created user
      log.Println(string(text))
      fmt.Fprintf(w, string(text))
      return
    }

    // if not -1, we rename
    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    new_user := create_user(user.Name)

    text, err := json.Marshal([]User{new_user})
    if (err != nil) {
      // rip
      return
    }
    // prints id of the created user
    log.Println(string(text))
    fmt.Fprintf(w, string(text))
}

func handler_group_messages(w http.ResponseWriter, r *http.Request) {
    log.Println("messages")

    // GET request

    params := strings.Split(r.URL.Path,"/")
    group_id := params[len(params)-1] // gets trailing part
    group_id_int, err := strconv.Atoi(group_id)

    all_messages := get_messages(group_id_int)

    text, err := json.Marshal(all_messages)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_group_messages_send(w http.ResponseWriter, r *http.Request) {
    log.Println("send message")

    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    decoder := json.NewDecoder(r.Body)
    datas := []struct {
      Body string
      GroupId int
      UserId int64
    }{}

    err := decoder.Decode(&datas)

    if(err != nil) {
      fmt.Fprintf(w, string(err.Error()))
    }

    data := datas[0]

    message_id := create_message(data.Body, data.GroupId, data.UserId)

    if (message_id > 0) {
      fmt.Fprintf(w, string(SUCCESS_MESSAGE))
    }
}

func getURLParam(path string, prefix string) []string {
  // currently returns trailing param, int only
  s := strings.SplitAfter(path, prefix)
  return s
}

func handler_notifications(w http.ResponseWriter, r *http.Request) {
    //log.Println("notifications")

    params := strings.Split(r.URL.Path,"/")
    user_id := params[len(params)-1] // gets trailing part
    user_id_int, err := strconv.Atoi(user_id)

    all_notifications := get_notifications(user_id_int)

    text, err := json.Marshal(all_notifications)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_groups_for_user(w http.ResponseWriter, r *http.Request) {
    log.Println("groups for user")

    params := strings.Split(r.URL.Path,"/")
    user_id := params[len(params)-1] // gets trailing part
    user_id_int, err := strconv.Atoi(user_id)

    groups := get_groups_for_user(user_id_int)

    text, err := json.Marshal(groups)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

// startup

func main() {
    var ssl_path = "/var/www/group_chat_project/web/private/ssl/"
    var crt = ssl_path+"both_cert.crt"
    var key = ssl_path+"amber_nopass.key"


    // handler definitions for various api calls
    http.HandleFunc("/", handler) // default
    http.HandleFunc("/status", handler_status)
    http.HandleFunc("/app", handler_app_link)

    // group info
    http.HandleFunc("/group/all", handler_group_all)
    http.HandleFunc("/group/", handler_group_one)

    // creates new group
    http.HandleFunc("/group/new", handler_group_new)

    // deletes group
    http.HandleFunc("/group/delete", handler_group_delete)

    // get messages in a group
    http.HandleFunc("/group/messages/", handler_group_messages)

    // create message in a group
    http.HandleFunc("/group/messages/send", handler_group_messages_send)

    // // send message to group
    // http.HandleFunc("/group/send", handler_group_send)
    //
    // add person to group
    http.HandleFunc("/group/join", handler_group_join)

    // remove person from group
    http.HandleFunc("/group/kick", handler_group_kick)

    // create new user
    http.HandleFunc("/user/new", handler_user_new_or_rename)

    // check notifications for user
    http.HandleFunc("/notifications/", handler_notifications)

    // check groups for user
    http.HandleFunc("/user/groups/", handler_groups_for_user)


    err := http.ListenAndServeTLS(":443", crt, key, nil)

    if err != nil {
        log.Fatal("ListenAndServe: ", err)
    }

}
