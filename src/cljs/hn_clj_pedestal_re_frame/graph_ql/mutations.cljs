(ns hn-clj-pedestal-re-frame.graph-ql.mutations)

(def post
  "post($url:String!, $description:String!) {
    post(
      url: $url,
      description: $description
    ) {
      id
    }
  }")

(def vote
  "vote($link_id:Int!) {
    vote(
      link_id: $link_id
    ) {
      id
      link {
        id
      }
      user {
        id
      }
    }
  }")

(def signup
  "signup($email:String!, $password:String!, $name:String!) {
    signup(
      email: $email,
      password: $password,
      name: $name
    ) {
      token
    }
  }")

(def login
  "login($email:String!, $password:String!) {
    login(
      email: $email,
      password: $password
    ) {
      token
    }
  }")
