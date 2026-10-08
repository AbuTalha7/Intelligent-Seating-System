public class Student {
    public String id, name, email, className, section, subjectGroup;
    public boolean specialNeeds;

    public Student(String id, String name, String email, String className,
                   String section, String subjectGroup, boolean specialNeeds) {
        this.id = id; this.name = name; this.email = email;
        this.className = className; this.section = section;
        this.subjectGroup = subjectGroup; this.specialNeeds = specialNeeds;
    }

    public String groupKey() { return className + "-" + section; }

    @Override
    public String toString() { return id + " – " + name + " (" + className + " " + section + ")"; }
}
