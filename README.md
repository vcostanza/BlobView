BlobView is Java 8 GUI library which uses an XML-based resource system similar to Android.

## Setup

For use in other projects you must link to the JAR or create a module dependency on BlobView (if using [**IntelliJ IDEA**](https://www.jetbrains.com/idea/)).

To initialize BlobView in your app you must create a `res` package somewhere in your project containing a static class for setting
up the resource loader (i.e. `MyProjectResources.java`).

Inside the class add a single static method (i.e. `init`) that calls:

```
public static void init() {
    Resources.addResourceClass(MyProjectResources.class);
}
```

Then simply call that method when starting your app. This will allow BlobView to lookup resources in your `res` package.

## Creating Resources

Resources are placed in the `res` directory under a set of sub-directories based on the resource type.

There are currently 4 directories with corresponding resources types:
- `layout` - Views and layout XML
- `icons` - Icons or images
- `values` - Miscellaneous resource values
- `html` - HTML files displayed locally (for use with the `HTMLDialog` class)

For example, to create the main layout for your app you'd create an XML file under `res/layout`.

## Layout Resources

BlobView uses a very similar syntax to [**Android**](https://developer.android.com/guide/topics/ui/declaring-layout) for layouts and resources.
The main difference is that traditional Java Swing components can be utilized within your layout XML in addition to
recreations of commonly used Android view classes such as `LinearLayout`.

Also the `android:` and `android:layout_` attribute prefixes do not need to be specified. For example:

`android:layout_width="wrap_content"` is represented in BlobView as simply `width="wrap_content"`.

BlobView also supports the `<include>` tag for including layouts within other layouts:

```
<include name="mainLayout" layout="@layout/layout_name"/>
```

## Resource Values

There are currently 3 types of resources placed in the `res/values` directory:
- `color.xml` - Color definitions
- `dimen.xml` - Size definitions
- `style.xml` - Style definitions which may utilize the resources above

These can be referenced using the `@color/`, `@dimen/`, `@style/`, etc. prefixes similar to Android.

## Accessing Layouts

As is the case with Android, layouts are created using a `LayoutInflater`, although using a slightly different syntax:

```
InflatedLayout inf = LayoutInflater.inflate("layout_name");
LinearLayout content = inf.findByName("content");
TextView title = inf.findByName("title");
```

You simply call `LayoutInflater.inflate(String)` with the name of your layout and retrieve views using the `findByName`
method on the `InflatedLayout` instance that's returned.

## Custom Views

You can reference custom-made views or components in your layout XML by specifying the full classname in the XML tag
or, more conveniently, by registering the class package to the `LayoutInflater`:

```
LayoutInflater.addPackageRoot("com.my.app.view");
```

In this example the custom views are stored in the `com/my/app/view` package. If a class in that package such as
`MyView.java` exists then you can simply reference it in your layout XML code using `<MyView name=.../>`.

These calls only need to be made once on the static instance of the `LayoutInflater` class. You can stick these in your
resource initializer class alongside the `Resources.addResourceClass` method.

## Themes

BlobView comes with a more modern looking dark theme if you're (understandably) not a fan of the default Swing theme. Simply call the following when starting your app:

```
DarkTheme.apply();
```