# gatherer-scraper
A tool used to automatically search [Gatherer](https://gatherer.wizards.com/) for information
regarding the cards you specify. Relevant information about the cards are saved to files in a way
that allows easy lookup.

## Behavior
Given a list of cards names specified in the "CardsToSearchFor.txt" file, each new card will be
searched for on Gatherer, cached locally to avoid prevent duplicate calls in the future, and then
added to a file in the "output/cards" directory to a file with the same name as its color identity.

After this is complete, if the `print-valid-commanders` property is set, all the cards that could be
commanders and have the color identity specified by the `print-valid-commanders.color-identity`
property will be displayed on the console.

If the syncing with Google Keep functionality is enabled, Google Keep will be queried for a note
called "Cards" with a "mtg" label; if found, the content of that note will be added to the
"CardsToSearchFor.txt" file. Afterward, Gatherer is called as it normally is to retrieve the
information for each of those cards. Then, unless the `sync-with-keep.master-file-only` property
is true, each file in the "cards" directory will cause a note of the same name to be created in
Google Keep.

**A note of caution:** prior to syncing the relevant files to Google Keep (if enabled), all preexisting
notes with the "mtg" label will be deleted besides the `Cards` note.


## Requirements
[gecko web driver](https://github.com/mozilla/geckodriver/releases)

Once the gecko web driver is downloaded, the `webdriver.gecko.driver.location` property should be updated
to point to the executable.

To use the functionality of syncing cards with Google Keep the Python Requirements must also be met.

### (Optional) Python Requirements
[python 3](https://www.python.org/downloads/).

[pip](https://pip.pypa.io/en/stable/installing/) (Bundled with python 3.4 and greater)

Once pip is installed, the Google Keep API and keyring packages need to be installed via:

`pip install gkeepapi`

`pip install keyring`