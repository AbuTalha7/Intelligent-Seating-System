import java.util.*;

public class ConflictGraph {
    public Map<String, Set<String>> adj = new HashMap<>();

    public void build(List<Student> list) {
        adj.clear();
        for (Student s : list)
            adj.put(s.id, new HashSet<>());
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                Student a = list.get(i), b = list.get(j);
                if (conflict(a, b)) {
                    adj.get(a.id).add(b.id);
                    adj.get(b.id).add(a.id);
                }
            }
        }
    }

    private boolean conflict(Student a, Student b) {
        return a.className.equals(b.className) && a.section.equals(b.section);
    }

    public boolean areConflicting(String id1, String id2) {
        Set<String> n = adj.get(id1);
        return n != null && n.contains(id2);
    }

    public int degree(String id) {
        Set<String> n = adj.get(id);
        return n == null ? 0 : n.size();
    }
}
