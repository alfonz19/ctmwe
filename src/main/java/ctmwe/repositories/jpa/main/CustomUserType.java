package ctmwe.repositories.jpa.main;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

public class CustomUserType implements UserType {

    public static final CustomUserType INSTANCE = new CustomUserType();

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.STRUCT};
    }

    @Override
    public Class returnedClass() {
        return CustomRecordDTO.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x instanceof String[] && y instanceof String[]) {
            return Arrays.deepEquals((String[])x, (String[])y);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return Arrays.hashCode((String[])x);
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {

        throw new IllegalStateException("totally no idea what could be here");
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value != null && st != null) {
            if (value instanceof CustomRecordDTO crdto) {
                Struct struct = session.connection()
                        .createStruct("AAA.CUSTOM_RECORD", new Object[]{crdto.getValue1(), crdto.getValue2()});
                st.setObject(index, struct);
            } else {
                throw new IllegalStateException();
            }
        } else {
            st.setNull(index, sqlTypes()[0]);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        String[] a = (String[])value;
        return Arrays.copyOf(a, a.length);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
