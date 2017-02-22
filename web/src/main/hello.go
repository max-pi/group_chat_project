package main

import (
    "fmt"
    "net/http"
    "log"
    "github.com/go-sql-driver/mysql"
)


func handler(w http.ResponseWriter, r *http.Request) {
    fmt.Fprintf(w, "Hi there, I love %s!", r.URL.Path[1:])
}

func main() {
    var ssl_path = "/var/www/group_chat_project/web/private/ssl"
    var crt = ssl_path+"2_erf.io.crt"
    var key = ssl_path+"amber_nopass.key"

    http.HandleFunc("/", handler)
    err := http.ListenAndServeTLS(":443", crt, key, nil)

    if err != nil {
        log.Fatal("ListenAndServe: ", err)
    }
}
