# Documentation:
# https://github.com/kiwiz/gkeepapi/blob/master/docs/index.rst
# To set up token (1st time use):
# https://github.com/kiwiz/gkeepapi/blob/master/docs/index.rst#logging-in

import getopt
import os
import sys

import gkeepapi
import keyring
from typing import List


def initialize_keep(email: str) -> gkeepapi.Keep:
    local_keep = gkeepapi.Keep()

    # Login
    token = keyring.get_password('google-keep-token', email)
    local_keep.resume(email, token)

    return local_keep


def find_or_create_label(name: str) -> gkeepapi.node.Label:
    mtg_label = keep.findLabel(name)
    if mtg_label is None:
        mtg_label = keep.createLabel(name)

    return mtg_label


def delete_all_old_notes(label: gkeepapi.node.Label, excluded_titles: List[str]):
    # Delete All Existing MTG Labels
    mtg_notes = keep.find(labels=[label])
    for mtg_note in mtg_notes:
        if mtg_note.title not in excluded_titles:
            mtg_note.delete()


def create_keep_note(title: str, content: List[str], label: gkeepapi.node.Label = None):
    note = keep.createNote(title, ''.join(content))
    if label is not None:
        note.labels.add(label)


def parse_opts_is_master_file_only() -> bool:
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hm", ["masterFileOnly"])
    except getopt.GetoptError:
        print(sys.argv[0] + ' -m/--masterFileOnly')
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print(sys.argv[0] + ' -m/--masterFileOnly')
            sys.exit()
        elif opt in ("-m", "--masterFileOnly"):
            return True

    return False


def call_from_java():
    print("called from java")


def add_keep_files_to_cards_to_search_for():
    mtg_label = find_or_create_label('mtg')

    try:
        new_cards_note = next(keep.find(labels=[mtg_label], query='Cards'))
        new_cards = new_cards_note.text.splitlines(True)

        output_file_name = os.path.join('.', 'src', 'main', 'resources', 'CardsToSearchFor.txt')
        with open(output_file_name, 'a') as output_file:
            output_file.write('\n')
            output_file.writelines(new_cards)
    except StopIteration:
        pass


def main():
    master_file_only = parse_opts_is_master_file_only()

    mtg_label = find_or_create_label('mtg')
    delete_all_old_notes(mtg_label, ['Cards'])

    file_names = []
    if master_file_only:
        master_file_name = os.path.join('.', 'output', 'Master.txt')
        file_names.append(master_file_name)
    else:
        card_directory = os.path.join('.', 'output', 'cards')
        short_file_names = os.listdir(card_directory)
        file_names = [os.path.join(card_directory, short_file_name) for short_file_name in short_file_names]

    for file_name in file_names:
        with open(file_name, 'r') as file:
            short_file_name = os.path.basename(file_name)
            note_name = os.path.splitext(short_file_name)[0]
            note_contents = file.readlines()

            # Note: Apparently Keep notes have a size limit.
            # Apparently, creating notes with this API can bypass that limit.
            # Obviously, this may be subject to change...
            create_keep_note(note_name, note_contents, mtg_label)

    keep.sync()


keep: gkeepapi.Keep = initialize_keep('emailAddress@gmail.com')

if __name__ == "__main__":
    main()
