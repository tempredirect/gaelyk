new AntBuilder().sequential {

    // current Gaelyk version
    version = '0.4'

    // various directory places and file names
    src = "src/main"
    testSrc = "src/test"
    lib = "lib"

    target = "target"
    classes = "${target}/classes"
    testClasses = "${target}/test-classes"
    testReports = "${target}/test-reports"

    jarname = "${target}/gaelyk-${version}.jar"

    tmpProj = "../template-project"
    zipname = "${target}/gaelyk-template-project-${version}.zip"
    projLib = "${tmpProj}/war/WEB-INF/lib"

    apidir = "../website/war/api"

    if (!args) {
        echo "Usage: groovy build (jar|template|javadoc|dist|clean|test)"
    } else {
        action = args[0]

        echo "Target: $action"

        if (action == 'clean'){
            delete dir: target
        }
        if (action in ['jar', 'template', 'dist']) {
            echo "Creating classes directory"
            mkdir dir: classes
            mkdir dir: testClasses

            echo "Compiling Gaelyk sources"
            taskdef name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc"
            groovyc srcdir: src, destdir: classes, {
                classpath {
                    fileset dir: lib, {
                        include name: "*.jar"
                    }
                    pathelement path: classes
                }
                javac source: "1.5", target: "1.5", debug: "on"
            }
            echo "Compiling Gaelyk test sources"
            taskdef name: "groovyc", classname: "org.codehaus.groovy.ant.Groovyc"
            groovyc srcdir: testSrc, destdir: testClasses, {
                classpath {
                    fileset dir: lib, {
                        include name: "*.jar"
                    }
                    pathelement path: classes
                }
                javac source: "1.5", target: "1.5", debug: "on"
            }

            echo "Creating the Gaelyk JAR"
            jar basedir: classes, destfile: jarname
        }
        if (action in ['dist','test']){
            delete dir:testReports
            mkdir dir:testReports
            echo "Running unit tests"
            junit fork:false, printsummary:true, haltonfailure:true, {
                classpath {
                    fileset dir: lib, {
                        include name: "*.jar"
                    }
                    pathelement path: classes
                    pathelement path: testClasses
                }
                batchtest todir:testReports, {
                    fileset dir:testClasses, {
                        include name:"**/*Test.class"
                    }
                }
            }
        }
        if (action in ['template', 'dist']) {
            echo "Deleting old Gaelyk JARs from the template project"
            delete {
                fileset dir: projLib, includes: "gaelyk-*.jar"
            }
            echo "Copying the latest Gaelyk JAR in the template project"
            copy file: jarname, todir: projLib

            echo "Creating the template project ZIP file"
            zip basedir: tmpProj, destfile: zipname, excludes: '__MACOSX, *.iml'
        }

        if (action in ['javadoc', 'dist']) {
            echo "Creating the GroovyDoc"
            taskdef name: 'groovydoc', classname: 'org.codehaus.groovy.ant.Groovydoc'
            groovydoc destdir: apidir,
                    sourcepath: src,
                    packagenames: "**.*",
                    windowtitle: "Gaelyk ${version}",
                    doctitle: "Gaelyk ${version}", {
                        link packages: 'javax.servlet.', href: 'http://java.sun.com/javaee/5/docs/api/'
                        link packages: 'java.,org.xml.,org.xml.', href: 'http://java.sun.com/j2se/1.5.0/docs/api'
                        link packages: 'com.google.appengine.', href: 'http://code.google.com/appengine/docs/java/javadoc/'
                        link packages: 'org.codehaus.groovy.,groovy.', href: 'http://groovy.codehaus.org/gapi/'
                    }
        }

        echo "Done."
    }
}