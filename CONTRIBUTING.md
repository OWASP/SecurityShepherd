
# OWASP Security Shepherd Contributing

## GitFlow
Shepherd uses [GitFlow](https://datasift.github.io/gitflow/IntroducingGitFlow.html). That basically means you never directly commit to master / dev.

## Where do I put new code?
To add a new feature or fix a bug in Shepherd, create a fork or branch from the [dev branch](https://github.com/OWASP/SecurityShepherd/tree/dev). When you're branch is complete and your JUnit's have been created / run clear, create a pull request to merge your branch into dev. Squash your commits if you like, if you don't that will be done be at merge.

## Branch Naming Convention
If you're working on an issue from the backlog, call your branch dev#{issueNumber}

## Code Format
Shepherd uses [Google's Java format styleguide](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml). Please ensure your IDE will auto format to this spec before you merge.

## How do I see the Backlog?
Install ZenHub for your browser and click the ZenHub tab that will appear in this repo. The Pipelines are as follows
1. *New Issues* - Issues yet to be reviewed for priority
2. *Ice Box* - Issues that are valid, but have not been prioritized for the backlog
3. *Backlog* - The Backlog order for priority.
4. *In Progess* - Items that are currently being worked
5. *QA Review* - Issues that have pull requests and require review / approval
6. *Closed* - Item is Done

## How do I setup my dev environment?
[Like This](https://github.com/OWASP/SecurityShepherd/wiki/Create-a-Security-Shepherd-Dev-Environment)

## Is there a Definition of Done?
*Work in Progess*  
- [ ] New Code has 'Good' JUnit Tests that cover it
- [ ] All JUnit Tests Pass
- [ ] Acceptance Criteria of Epic has been satisfied where applicable
- [ ] Code does not introduce a vulnerability that can be leveraged to exploit the system/other users
