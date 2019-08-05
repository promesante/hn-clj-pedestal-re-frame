(ns hn-clj-pedestal-re-frame.graph-ql.queries)

(def feed
  "FeedQuery($first: Int, $skip: Int) {
    feed(
      first: $first,
      skip: $skip
    ) {
      count
      links {
        id
        created_at
        url
        description
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
    }
  }")

(def search
  "FeedSearchQuery($filter: String!) {
    feed(filter: $filter) {
      links {
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
    }
  }")
