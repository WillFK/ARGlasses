#AR experiment

### Tech Stack
AR Core + Sceneform

Making use of AR Core as it was a easy way of tracking the user face.
The library seems to be performing well. The tracking is fast and precise and I faced no major issues in the course of development.

Sceneform as a rendering engine was a difficult choice. When researching which technologies I should use for this challange I found less tutorials and examples than I expected.
Sceneform was able to render GLTF files in a simple way, manage if the AR service is installed correctly (and prompt the user to updare it if not) and seemed to be easy to integrate with AR Core, and those are the ponts that made me choose the library.
The problem is that it has been archived by Google. Filament seems to be the successor to Sceneform, but I have found even less content online to guide me.

I considered using Unit/Vuforia, but gave up on this idea due to the lack with familiarity with those tools and the short time available to finish the challange.

I avoid using libraries other than those. So no Coroutines, RxJava, Dagger or Navigation.

### Architecture

The choosen architecture for this project was MVI.
In this model, the UI state is represented by the `GlassesState` object. To handle events that may be consumed and not triggered again I'm using `GassesMessage`.

I'm happy with the result of the architecture. In the beginning it felt a bit overengineering due to the high number of classes I need to get things working (fragment, viewmode, actions, state, message, reducer) but as develpment proceded things felt very organized, which is very helpful when making changes and hunting for bugs. Also is very friendly for changes and testing.

### Tests

No tests where implemented due to time constraints.
If I where to implement them, I would create statand unit tests for `GlassesReducer` and UI tests for `GlassesFragment` (mostly to cover the logic behind hiding and showing elements).

I'm not sure how to best test the AR funcionality. I didn't find a good tool to emulate the AR part of the application, but I belive a more careful research could yield better results.

### Development process

The project was implemented in 2 days, one which was used for research and conceptualizing what had to be implemented and another one for implementation.

### Final thoughts

Given the time and lack of experience with the technology, I'm satisfyed with the results.
The app is running smoothly and the tracking is precise.

But clearly there are some problems. Using AR Core means that devices that don't support it wont be able to run the application. There's also some problems with the rendering itself, like the lack of occlusion. I belive there are some tools that could me help with the latter, like the recently released Depth API by google.

The rendering engine is not ideal since it has been deprecated and should be replaced. After google archived it, the community forked the project kept supporting it. Maybe the community version would be a good option here. We could also opt to use Filament.
