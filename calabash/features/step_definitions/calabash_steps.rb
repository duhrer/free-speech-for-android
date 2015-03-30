# The default steps defined by calabash-android
require 'calabash-android/calabash_steps'

# Our custom steps

# Steps to handle listening for audio output
require File.dirname(__FILE__) + '/sound_steps'

# Steps to simplify working with preferences
require File.dirname(__FILE__) + '/preferences_steps'

# Steps to simplify working with tools
require File.dirname(__FILE__) + '/tools_steps'

# Steps to replace the broken built-in l10n handling
require File.dirname(__FILE__) + '/l10n_steps'