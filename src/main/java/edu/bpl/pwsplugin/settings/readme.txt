This package is used to contain settings classes. These classes should all inherit from 
`JsonableParam` to make saving and loading from JSON easier. These classes don't really
do anything, they are just used to convey settings about various aspects of the program.
For example, many parts of the UI can load these settings and can also produce new versions
of settings which are then used to actually execute an action.
