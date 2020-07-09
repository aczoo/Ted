# Ted
https://hackmd.io/@zxzMJn9cTR2JoeKMnPiT_Q/SkJXzaj0I

 * Home page displays recent news, videos, and petitions
    * News API :https://newsapi.org/docs/endpoints/sources
       * response variables have a specified category, which could be used to narrow down of legal news
 * What area of Legal Practice should I narrow in on?
        * Quick Legal Advice about individual rights
            * Reddit API to search for posts under the subreddit “r/legaladvice“, may be faster than using the court document APIs
            * https://www.justia.com/criminal/criminal-faqs/
            * First Amendment: Rights to Religion, Speech, Press, Assembly, Petition
                * Supreme Court Decisions: https://www.freedomforuminstitute.org/first-amendment-center/supreme-court-cases/
                * Court Case API: https://case.law/api/
                * Legal Data API: https://unicourt.com/features/legal-data-api
        * Data Privacy 
            * Data Privacy Compliance API from Wolfram Language: https://www.programmableweb.com/api/data-privacy-compliance
    * Since most of the readily available information is simply factual, how will I add the conversational aspect?
        * Coggle? Is that too direct? Depends on the scope of the project?
* Main components of the project
    * Facebook Authentication
    *  Dialogflow integration for NLP
        * Google’s natural language understanding tool for building conversational experiences, such as voice apps and chatbots, powered by AI.
        * https://medium.com/@abhi007tyagi/android-chatbot-with-dialogflow-8c0dcc8d8018
        * https://github.com/dialogflow/dialogflow-android-client
    * Firebase Realtime DB for persisting chat messages
        * https://medium.com/@ritikjain1272/firebase-and-dialogflow-with-react-native-made-easy-301891afc4fb

