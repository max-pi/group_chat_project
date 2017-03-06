package main

import (
    "fmt"
    "net/http"
    "log"
    "strconv"
    "encoding/json"
    "strings"
)


// handlers for the api calls

func handler(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "Hi there, I love %s! \n", r.URL.Path[1:])
}

func handler_status(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "looks like we are ok")
}

func handler_app_link(w http.ResponseWriter, r *http.Request) {
    // TODO: redirect to the app store link
    fmt.Fprintf(w, "app store link:")
}

func handler_group_all(w http.ResponseWriter, r *http.Request) {

    all_groups := get_group_all()

    text, err := json.Marshal(all_groups)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_group_one(w http.ResponseWriter, r *http.Request) {
    if r.Method != "GET" {
      // only allow POST requests
      return
    }

    params := strings.Split(r.URL.Path,"/")

    group_id := params[len(params)-1]
    group_id_int, err := strconv.Atoi(group_id)

    group := get_group_with_id(group_id_int)

    text, err := json.Marshal(group)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    // show the group info
    fmt.Fprintf(w, string(text))
}

func handler_group_new(w http.ResponseWriter, r *http.Request) {
    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    group_name := r.PostFormValue("group_name")

    create_group(group_name)
}

func handler_group_delete(w http.ResponseWriter, r *http.Request) {
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
    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    group_id := r.PostFormValue("group_id")
    group_id_int, err := strconv.Atoi(group_id)

    user_id := r.PostFormValue("user_id")
    user_id_int, err := strconv.Atoi(user_id)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    join_group(group_id_int, user_id_int)
}

func handler_group_kick(w http.ResponseWriter, r *http.Request) {
    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    group_id := r.PostFormValue("group_id")
    group_id_int, err := strconv.Atoi(group_id)

    user_id := r.PostFormValue("user_id")
    user_id_int, err := strconv.Atoi(user_id)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    kick_user(group_id_int, user_id_int)
}


func handler_user_new(w http.ResponseWriter, r *http.Request) {
    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    user_name := r.PostFormValue("user_name")

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    create_user(user_name)

}

func handler_group_messages(w http.ResponseWriter, r *http.Request) {
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

    all_messages := get_messages(group_id_int)

    text, err := json.Marshal(all_messages)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_group_messages_send(w http.ResponseWriter, r *http.Request) {
    if r.Method != "POST" {
      // only allow POST requests
      return
    }

    r.ParseForm()
    body := r.PostFormValue("body")

    group_id := r.PostFormValue("group_id")
    group_id_int, err := strconv.Atoi(group_id)

    user_id := r.PostFormValue("user_id")
    user_id_int, err := strconv.Atoi(user_id)

    if (err != nil) {
      log.Fatal(err.Error())
      return
    }

    create_message(body, group_id_int, user_id_int)
}

func getURLParam(path string, prefix string) []string {
  // currently returns trailing param, int only
  s := strings.SplitAfter(path, prefix)
  return s
}




// startup

func main() {
    var ssl_path = "/var/www/group_chat_project/web/private/ssl/"
    var crt = ssl_path+"2_erf.io.crt"
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
    http.HandleFunc("/group/messages", handler_group_messages)

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
    http.HandleFunc("/user/new", handler_user_new)


    err := http.ListenAndServeTLS(":443", crt, key, nil)

    if err != nil {
        log.Fatal("ListenAndServe: ", err)
    }

}
