# income-tax-tailor-returns-fronted

This is where users can review and make changes to their income tax return.

## Running the service locally

You will need to have the following:
- Installed [MongoDB](https://docs.mongodb.com/manual/installation/)
- Installed/configured [service manager](https://github.com/hmrc/service-manager).

The service manager profile for this service is: (complete journey)

    sm2 --start INCOME_TAX_TAILOR_RETURNS_FRONTEND
Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_SUBMISSION_ALL -r

The tailoring private beta service manager profile for this service is: (reduced journey)

    sm2 --start INCOME_TAX_TAILOR_RETURNS_FRONTEND_PRIVATE_BETA

Run the following command to start the remaining services locally:

    sudo mongod (If not already running)
    sm2 --start INCOME_TAX_TAILOR_RETURNS_ALL -r

This service runs on port: `localhost:10007`

### Running Tests

- Run Unit Tests:  `sbt test`
- Run Integration Tests: `sbt it/test`
- Run Unit and Integration Tests: `sbt test it/test`
- Run Unit and Integration Tests with coverage report: `sbt runAllChecks`<br/>
  which runs `clean compile scalastyle coverage test it/test coverageReport`

### .g8 folder contains all the scaffold page types which you can use to create a new pages, to create the pages you need to enter the sbt shell

sbt shell
then select the page type and provide the class name
After creating the pages using the script come out of sbt shell and run ./migrate.sh script which will generate the relevant test cases.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
