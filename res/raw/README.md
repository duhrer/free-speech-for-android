# Welcome to the Documentation for Free Speech

These pages contain the draft documentation for Free Speech based on questions from end users.

# Getting Started

To run Free Speech, you will need an Android device which:

1. Is running Android Gingerbread or higher
2. Has a [text to speech engine](#markdown-header-text-to-speech) and at least one installed voice.

## Text-to-speech

By default, Free Speech relies on your device having at least one text-to-speech engine.  Most devices will include one, but if you don't have one (or don't like the one you have) there are also a number of third-party engines of note:

1. [Ivona TTS](https://play.google.com/store/apps/details?id=com.ivona.tts&hl=en)
2. [SVOX classic](https://play.google.com/store/apps/details?id=com.svox.classic&hl=en)

Both Ivona and SVOX have a range of voices for different languages and regions, and offer both male and female voices.

Free Speech relies on your device's TTS engine and configuration, so it's a good idea to familiarize yourself with the text-to-speech settings on your device.  In Android Lollipop, these are generally found under the "Accessibility" menu under "Text-to-speech output".

The location of these settings varies from Android version to version, and may also have been updated by the manufacturer of your device.  If you're having trouble finding your text-to-speech settings, check the documentation that came with your device.

# Installing Free Speech

Most people should [install Free Speech through the Google Play Store](https://play.google.com/store/apps/details?id=com.blogspot.tonyatkins.freespeech).

# Using Free Speech

In general, Free Speech consists of a series of tabs, which can be accessed using the tab links at the top of the screen.  Each tab consists of one or more buttons.  Each button will say something, or navigate to another tab, or both (see "[Using Links](#markdown-header-using-links)" below).

# Enabling Editing

To add or edit content in Free Speech, you will need to use the menu key (or onscreen menu indicator) and go to the "Preferences" tab.  Once you're there, scroll down and check the "Allow Editing" box to enable editing, then use the "back" button to exit the "Preferences" activity.

# Adding/Editing Tabs

You can only add/edit tabs if you have [enabled editing](#markdown-header-enabling-editing) as described above.

To add a new tab, hit the menu button or onscreen menu indicator, then select the "Add" icon.  A menu will appear.  Select "Add Tab" to add a new tab.

To edit an existing tab, press and hold down on its title at the top of the screen.  A menu will appear, click "Edit Tab" to edit the tab.

Whether you are adding or editing a tab, you will see two options.  The first is the tab label, which will appear in the tab bar at the top of the screen.  This should be relatively short, no more than 8-10 characters.  You will also see the option to select a background color for the tab.

# Adding/Editing Buttons

You can only add/edit buttons if you have [enabled editing](#markdown-header-enabling-editing) as described above.

To add a new tab, hit the menu button or onscreen menu indicator, then select the "Add" icon.  A menu will appear.  Select "Add Button" to add a new tab.

To edit an existing button, press and hold down the button.  A menu will appear, click "Edit Button" to edit the button.

Whether you are adding or editing a tab, you will see three main categories of options:

1. [What to Show](#markdown-header-configuring-what-to-show-for-a-button)
2. [What to Say](#markdown-header-configuring-what-to-say-for-a-button)
3. [What to Do](#markdown-header-configuring-what-to-do)

Read on for details about each section

## Configuring "What to Show" for a Button

A button can display a text label, an image, or both.  A button can also have its own background color.  All of these options are controlled in the "What to Show" section of the button add/edit interface.

### Adding an Image to a Button

If you would like to display an image on a button, use the "Image" controls in the "What to Show" section.  

To select an existing image from your device, use the "picture" icon.  Any "gallery" apps you have installed will appear on the list.  If you have no "gallery" app, Free Speech will appear on the list.  If you use Free Speech to pick a file, you will be presented with a file navigator that you can use to find images.  Tap a directory to change directory.  Tap an image to select that image.

To take a new photo, use the "camera" icon.  Your device's camera app will launch, and will return to Free Speech once you have taken a picture and confirmed that you like it (usually, using a checkbox).

Once you have taken a photo or selected an image, you will see additional image controls on the right side of the image.  You can use these controls to crop an image or to rotate it 90 degrees left or right.

## Configuring "What to Say" for a Button

A button can read text using the [text-to-speech engine](#markdown-header-text-to-speech), can play a sound, or can simply act as a link between tabs (see "[Using Links](#markdown-header-using-links)" below).

If you would like to have the text-to-speech engine read text, select "Speak Text", and enter the text you would like to have spoken.

If you would like to play a sound, select "Play Sound".  You will see two buttons, one with a microphone, and another with the word "File" on it.  If you would like to play an existing sound, use the "File" button to choose the sound file to play.  If you would like to record a new sound, press the microphone button and follow the instructions under "[Recording Sounds](#markdown-header-recording-sounds)".

## Configuring "What to Do" for a Button

The "What to Do" section controls whether the button navigates to another tab (see "[Using Links](#markdown-header-using-links)" below for more information)

# Preferences

TODO:  this section coming soon.

# Advanced Options

This section describes more advanced options.

## Changing the Order of Tabs

To change the order in which tabs appear, use the menu button or onscreen menu icon, then select "Sort".  From the menu that appears, select "Sort Tabs".  A screen will appear which will allow you to drag and drop the tabs into the order you wish.  To save the new order, press the "Done" button.  To abandon your changes, use the "back" button.

## Changing the Order of Buttons

To change the order in which buttons appear on a tab, navigate to the tab, then use the menu button or onscreen menu icon, and select "Sort".  

From the menu that appears, select "Sort Buttons".  A screen will appear which will allow you to drag and drop the buttons into the order you wish.  To save the new order, press the "Done" button.  To abandon your changes, use the "back" button.

## Moving a Button from One Screen to Another

If you would like for a button to appear on another tab, press and hold on the button until the menu appears, then select "Move Button to Another Tab" on the dialog that appears.  You will be presented with a list of tabs, choose the tab you would like to move the button to.

## Recording Sounds

There are many Android applications that help in recording sounds.  Many devices have a sound recorder included.  When you press the "microphone" button while adding/editing a button, you will be prompted to pick an installed sound recorder.

If you don't have another sound recorder installed, Free Speech will be listed as the only option.

If you are using a third-party recording app, consult its documentation.  You should be returned to Free Speech when you finish recording a sound.

If you are using the build-in recording activity, you should see an activity with a timer and three buttons with the "record" (circle), "play" (right-facing triangle), and "stop" (square) icons.  Press "record" to start recording, and "stop" to finish.  

Once you have recorded sound data, you can preview the content using the "play" button.  If you are happy with the recording, press "Save" to return to the button editing activity.  If you would like to cancel, press the back button or the "Cancel" button.

## Using Links

Links are one of the most advanced features of Free Speech, and also the most powerful.  Using links, you can configure a button to navigate to another tab.  In the demo data, links are used to move from the subject of the sentence ("I", "You", etc.) to a verb ("Want to", "Need to", "Feel", etc.) to a completing clause ("Go Outside", etc.).  These type of links allow you to construct hundreds of sentences in just a few clicks.

Links can also be used to navigate to a logical starting point for the conversation.  For example, the default data includes a "home" button on every tab that can be used to get back to the main tab.

To create a link, scroll down to the "What to do" section of the button editing screen (see "[Adding/Editing Buttons](#markdown-header-adding-editing-buttons)" above), and select the tab you wish to navigate to when the button is pressed.

# Questions or Problems

If you have questions about using Free Speech or experience problems, please get in touch by [creating an issue](https://bitbucket.org/duhrer/free-speech-for-android/issues/new).