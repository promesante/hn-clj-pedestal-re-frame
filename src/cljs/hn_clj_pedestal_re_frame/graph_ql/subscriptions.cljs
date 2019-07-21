(ns hn-clj-pedestal-re-frame.graph-ql.subscriptions
  )

(def new-link
  "{
    newLink {
      id
      url
      description
      created_at
      posted_by {
        id
        name
      }
    }
  }")

(def new-vote
  "{
    newVote {
      id
      link {
        id
        url
        description
        created_at
        posted_by {
          id
          name
        }
        votes {
          id
          user {
            id
          }
        }
      }
      user {
        id
      }
    }
  }")

