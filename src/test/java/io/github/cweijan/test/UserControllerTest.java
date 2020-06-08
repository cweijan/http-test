package io.github.cweijan.test;

import io.github.cweijan.mock.Asserter;
import io.github.cweijan.mock.boot.controller.UserMockController;
import io.github.cweijan.mock.boot.pojo.User;
import io.github.cweijan.mock.jupiter.HttpTest;
import io.github.cweijan.mock.request.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author cweijan
 * @since 2020/06/03 19:45
 */
@HttpTest
public class UserControllerTest {

    @Resource
    private UserMockController userController;

    @Value("${server.port}")
    private Integer port;

    public class D extends AbstractList<String> {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(String s) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public String get(int index) {
            return null;
        }

        @Override
        public String set(int index, String element) {
            return null;
        }

        @Override
        public void add(int index, String element) {

        }

        @Override
        public String remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<String> listIterator() {
            return null;
        }

        @Override
        public ListIterator<String> listIterator(int index) {
            return null;
        }

        @Override
        public List<String> subList(int fromIndex, int toIndex) {
            return null;
        }
    }

    @Test
    void list() {

        List<User> list = userController.list();
        System.out.println(list);

    }

    @Test
    void saveUser() {

        User user = Generator.request(User.class);
        Integer userId = user.getId();
        userController.saveUser(user);

        User savedUser = userController.getUser(userId);
        Asserter.assertSame(user, savedUser);

//        userController.deleteUser(userId);
//
//        User nowUser = userController.getUser(userId);
//        Asserter.assertNull(nowUser);

    }


}
