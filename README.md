# gcal2org

Gcal2org is a simple utility that uses the Google Calendar API to pull down events and writes an org-mode file with the events.

## Installation

The gcal2org script will download and install the binaries on the first run.

1. Download the gcal2org script from the latest release.
2. Place it in your `$PATH`.
3. Set it to be executable. (`chmod 755 gcal2org`)
4. Run it.

The first run, and any subsequent runs without Google API credentials, will give you instructions for creating and storing an API key and secret for accessing the Google API. Those instructions are also available below:

1. Go to https://console.developers.google.com/.
2. Click the blue "Create Project" button.
3. From the project dashboard, click "Enable an API" under "Boost your app with a Google API".
4. Enable the "Calendar API" from the list of Google APIs.
5. Navigate to "Credentials" under "APIs & auth".
6. Under OAuth, click "Create a new Client ID".
7. You want to create an ID for an "Installed Application". Fill out a name for the application (it can be anything).
8. You should see your client ID, click "Download JSON" to download the credentials.
9. Move the downloaded JSON file to `~/.gcal2org/client_secrets.json`.

More detailed directions on using the Google Developer Console can be found here: https://developers.google.com/google-apps/calendar/firstapp.

The script will need the client_secrets.json file to be located in `~/.gcal2org/client_secrets.json`. This location can be overriden using a command line argument.

#### Why do I have to go through all of these steps to create a Google API account?

I don't have a good way to control the volume through my own API keys and account and don't want to risk opening up an API key to potential charges. As a result, I request that you setup your own API credentials to use with this utility.

If anyone has a better way of handling this I'd be interested in hearing it!

## Usage

Basic usage requires the email address for your calendar, where to store your OAuth credentials, and the output org file.

    $ gcal2org -c "example@example.com" \
               -s "/home/example/.gcal2org/calendar.store" \
               -o "/home/example/Dropbox/org/calendar.org"

A full table of the command line arguments can be found below or by running `gcal2org -h`.

Command flag    | Description
----------------|--------------------------------------------------------
`-c/--calendar` | Email address of the Google Calendar you want to access.
`-d/--data`     | Base directory for gcal2org data.
`-s/--store`    | Location where Google Calendar credentials are stored.
`-o/--output`   | The file to be written, if left out it will write to stdout.
`-h/--help`     | Displays the command line options and description.
`--category`    | Org mode category for the .org calendar.

## License

Copyright © 2015 David Tulig

Distributed under the Eclipse Public License.

