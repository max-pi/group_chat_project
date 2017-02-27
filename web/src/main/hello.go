package main

import (
    "fmt"
    "net/http"
    "log"
    "strconv"
    "encoding/json"
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

    // if r.Method != "POST" {
    //     //http.ServeFile(res, req, "signup.html")
    //     return
    // }

    all_groups := get_group_all()

    text, err := json.Marshal(all_groups)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}

func handler_group(w http.ResponseWriter, r *http.Request) {
  if r.Method != "POST" {
    // only allow POST requests
    return
  }

    r.ParseForm()
    group_id := r.PostFormValue("group_id")
    group_id_int, err := strconv.Atoi(group_id)

    fmt.Fprintf(w, "yoyo")
    fmt.Fprintf(w, string(group_id_int))

    group := get_group_with_id(group_id_int)

    text, err := json.Marshal(group)

    if (err != nil) {
      // rip
      return
    }

    fmt.Fprintf(w, string(text))
}



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
    http.HandleFunc("/group/", handler_group)
    //
    // // creates new group
    // http.HandleFunc("/group/new", handler_group_new)
    //
    // // send message to group
    // http.HandleFunc("/group/send", handler_group_send)
    //
    // // add person to group
    // http.HandleFunc("/group/join", handler_group_join)
    //
    // // remove person from group
    // http.HandleFunc("/group/kick", handler_group_kick)

    err := http.ListenAndServeTLS(":443", crt, key, nil)

    if err != nil {
        log.Fatal("ListenAndServe: ", err)
    }

}
