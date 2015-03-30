# Replacements for the typo-riddled and broken steps included with calabash-android
Then /^I press text of translated l10nkey \"([^\"]+)\"$/ do |l10nkey|
  perform_action('press_l10n_element', l10nkey)
end

Then /^I press button of translated l10nkey \"([^\"]+)\"$/ do |l10nkey|
  perform_action('press_l10n_element', l10nkey,'button')
end

Then /^I press menu item of translated l10nkey \"([^\"]+)\"$/ do |l10nkey|
  perform_action('press_l10n_element', l10nkey,'menu_item')
end

Then /^I press toggle button of translated l10nkey \"([^\"]+)\"$/ do |l10nkey|
  perform_action('press_l10n_element', l10nkey,'toggle_button')
end

# This is taken verbatim from the source on Github, it's the only one that's not doubly screwed.
Then /^I wait for the translated \"([^\"]*)\" l10nkey to appear$/ do |l10nkey|
  perform_action('wait_for_l10n_element', l10nkey)
end