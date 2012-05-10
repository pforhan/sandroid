Summary
=======

This is a proposition for how to construct Android code using a strong MVC sensibility.  Since
View already has meaning in Android, and because Android reaches farther than only UI views,
think of it as a Model-Framework-Controller structure.

Purpose
=======

> "The only way to win is not to play"

For some reason Android is a very hostile development environment.  Some of this stems from
poor API design, others from the surrounding SDK and tools.  Two obvious examples are the
preference for abstract and concrete classes in place of interfaces, and the outright hostility
to testing in general.

To that end, I have constructed a lightweight philosophy for how to create android applications
whose logic is divorced from the Android code base.  This allows for a strict separation of 
view and controller and easy testability.

New Code - Controller and Display
=================================

Typically, for each activity, new code will consist of two interfaces and a class.  Supposing
an activity was named PaintActivity, there would be:
 - PaintController - interface defining all logic to be performed by an Activity.
 - PaintDisplay - interface defining operations that interact with the user and/or Android.
    Would normally be called PaintView, but this term is taken.
 - DefaultPaintController - implementation of PaintController that uses methods on PaintDisplay
    to perform its logic.

And of course, PaintActivity would implement PaintDisplay.

Mechanism
=========

Quite simply, all non-Android logic and state resides in the Controller implementation.  The
Activity is reduced to view interactions and Android system interactions only.

To convert an existing activity, start by moving its fields to the Controller.  Then generally,
for each logic block, create a corresponding method on Controller.  As needed, create methods
on the Display for the controller to call.

The Activity will flatten out to simple methods implementing the Display.  During onCreate, 
it will set itself on the Controller, and pass any information necessary from intents or 
bundles for the controller to initialize itself and the Activity.  It will attach all 
appropriate listeners as well.

A key tenant is to keep all Android-related classes and method calls within the activity.
Application-specific domain classes may be passed back and forth between Controller and Display.
Application-specific logic, however, should remain confined to the Controller.

Testing
=======

The Activity should be simplified to the point that it seems trivial.  For example, it is 
told to display a string in a field, and it does so.  There are no branching paths based on
custom application state.  It feels foolish to test. (Though full integration tests are worth
their own look.)

Rather than writing a testSetTitle_setsTitle() method on the Activity (since that's all it has
now), the meat of the testing is upon the controller implementation.  Simply provide it a stub
or mock Display implementation and you can write interesting tests like 
testWhenUserHasNoName_promptsUserForName, all while avoiding android testing restrictions (no
stub classes) or heavy-handed testing frameworks (sorry, Robolectric) 

Criticism
=========

A simple issue with this approach is naming.  As mentioned before, we can't use the familiar
"View" of MVC, and besides, with services and i18n and so much more offered by Android, it
is not really appropriate.  I chose to use 'Display' but that also seems scope-limited.
"Framework" is a bit more accurate but at the same time something like UserFramework or 
PaintFramework sounds a bit odd.  Is there a better name?

This proposal does add an extra layer to the software stack, though I maintain it is a 
lightweight and practical one.  Android seems to be trying to use fragments and sundries
to accomplish much the same goal -- the removal of logic locked into Activities.  This goes a
step farther.

Finally, there is no single framework or appropriate design.  Some controllers may have one
method, some ten.  And I'm sure the code will differ based on the author's personality.

Implementation
==============

This is a simple implementation of this philosophy based on the [Newsreader demo](http://developer.android.com/training/multiscreen/adaptui.html) from
the Android developer site.  This is a reasonably complex demo that performs a number of
layout and fragment tricks.  Still, it was a good starting point.  It has two activities,
one simple, one complex.  The fragments were thankfully just thin wrappers around their
views and didn't really have to be adjusted.

I split the app into two packages, .core, containing domain objects and logic, and .ui, which
held android classes extending Fragment, Activity, etc in commit [1cb581e4](https://github.com/pforhan/sandroid/commit/1cb581e49d02a75911c83bdd259a6b26c2f6dacd).  I then constructed a third package,
.mvc, into which I placed the new Controller and Display interfaces and implementations in commit
[d69cb03c](https://github.com/pforhan/sandroid/commit/d69cb03c79e43a6ad399a033d7635aec07b6a75f). 
I ripped out the non-controller code and connected to the controllers in commit [9e103a5f](https://github.com/pforhan/sandroid/commit/9e103a5fa1d14b4d7075c7d811f727d27de88909).
Reference comments with "SANDROID" to see special implementation notes I added.

