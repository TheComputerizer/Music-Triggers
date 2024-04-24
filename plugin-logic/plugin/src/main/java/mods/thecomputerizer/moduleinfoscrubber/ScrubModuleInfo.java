import org.gradle.api.tasks.AbstractCopyTask;
import org.gradle.api.tasks.Copy;

public abstract class ScrubModuleInfo extends Copy {

    @Override
    public AbstractCopyTask from(Object ... sources) { //TODO Implement this?
        return super.from(sources);
    }
}
