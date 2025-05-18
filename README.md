# blog-monitor
SpringBoot Temporal Schedule Example

Author: Nicholas Kum

Date: 18 May 2025

This is my solution for the AWS + Temporal hackathon.

This integrates with the free and open source writefreely blogging software https://writefreely.org which I installed locally and have customised the port to 8282.

The blog-monitor uses Scheduled Actions which is currently the recommended way to schedule Workflows in Temporal.
Refer: https://temporal.io/blog/temporal-v1-20-lighter-and-simpler-development-environments-and-improved?_gl=1*3tdjm3*_gcl_au*NjUwOTU5MTQ4LjE3NDI5Mzk5OTI.*_ga*MTU2Mjk5NTEwNi4xNzQyOTM5OTky*_ga_R90Q9SJD3D*czE3NDY2NjY2NTkkbzYkZzEkdDE3NDY2Njk4MTUkajAkbDAkaDA.

For the purposes of demonstration, the schedule is limited to 4 workflow executions, and has a workflow run timeout of 30 seconds.

The blog-monitor can be used to demonstrate durable executions in Temporal.
For example, if you stop the blog-monitor SpringBoot application after first the workflow execution and then re-start the blog-monitor SpringBoot application the schedule Workflows after the re-start will be able to process successfully and will remember the blog articles that were previously processed so that duplicate promotions are not created.
The Workflow can be implemented to make use of the lastCompletedResult feature in Temporal to pass state to the next Workflow execution.

The blog-monitor detects changes by recording a hash for each blog article.
If a blog article is added or updated the blog article is summarised and promoted as appropriate.
The blog-monitor does not consider an article being deleted as a change.

The blog-monitor hasn't implemented the feedback tasks in the hackathon but does integrate with GitHub discussions for API-related promotions.

***

Some configuration is required for the blog-monitor to run successfully:
1. Java and Maven - I used Java 17 but might also run on Java 8. I used Maven version 3.9.9.
2. Setup the AWS CLI on your laptop so that you can connect to the AWS Bedrock through the AWS CLI
3. AWS Bedrock has Titan Express available
4. Setup writefreely on your laptop.

   Update the blogUrl in GetBlogActivityImpl if necessary, I set my port to a non-default port of 8282.

5. Setup GitHub discussions on your GitHub repo and create a fine grained Personal Access Token that has read and write permission for the GitHub Discussions and find the Repository Id and Category Id for the GitHub discussion:

   https://docs.github.com/en/graphql/overview/explorer

   ```
   query {
      repository(owner: "YOUR_GITHUB_USER_NAME", name: "YOUR_REPOSITORY_NAME") {
        id # RepositoryID
        name
        discussionCategories(first: 10) {
          nodes {
            id # CategoryID
            name
          }
        }
      }
   }
   ```

   Update SendGitHubChannelActivityImpl token variable with your person access token and update the requestBody method with your repository Id and category Id.

6. Setup Temporal on your laptop.

   The application.yaml has Temporal setup as 127.0.0.1:7233

   Start Temporal:
   ```
   temporal server start-dev
   ```
   
***

To run the blog-monitor application using the CLI:

   ```
   mvn spring-boot:run
   ```

Disclaimer: If I had more time I would create an application-local.yaml file for local configurations, and also a application-private.yaml file which I would add to .gitignore to store secrets like the personal access token for GitHub Discussions.
It is never a good idea to push credentials to a GitHub repository.

***

An example blog article for an API-related promotion:

    Temporal API Cron Schedule has been superseded by Scheduled Actions.

An example blog article update for an API-related promotion:

    Temporal API Cron Schedule has been superseded by Scheduled Actions.
    Since version 1.20.0
