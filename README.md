# bumpversion plugin for Bamboo

Custom Task type to integrate bumpversion (https://github.com/peritus/bumpversion) with Bamboo.

This plugin provides:
- custom BumpVersion task to run bumpversion (with optional custom parameters)
- custom capability to enable bumpversion as build requirement

## How it works
Simply add `bumpversion.cfg` file to your repository and `BumpVersion` task to your build plan.
Plugin by default will use `patch` mode to increase last part of the version, but you can 
overwrite that setting by special commit message matching regular expression: `bumpversion::(patch|minor|major)`

Additionally plugin will expose new version as result variable: `version.bumped` which can be used 
in deployment plan as well as part of release number.

## Screenshots

