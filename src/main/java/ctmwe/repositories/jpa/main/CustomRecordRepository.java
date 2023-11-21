package ctmwe.repositories.jpa.main;

import lombok.AllArgsConstructor;
import oracle.sql.ARRAY;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;

import org.hibernate.Session;
import org.hibernate.jpa.TypedParameterValue;
import org.hibernate.procedure.ParameterRegistration;
import org.hibernate.procedure.ProcedureCall;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class CustomRecordRepository {

    private final EntityManager em;


    public void callWithSingleCustomType(CustomRecordDTO dtos) {
        Session session = em.unwrap(Session.class);
        ProcedureCall proc = session.createStoredProcedureCall("Process_Custom_Record");
        ParameterRegistration<ARRAY> registeredParameter = proc.registerParameter("p_list", ARRAY.class, ParameterMode.IN);
        proc.setParameter(registeredParameter, new TypedParameterValue(CustomUserType.INSTANCE, dtos));

        proc.execute();
    }

    public void callUsingListOfCustomTypes(List<CustomRecordDTO> dtos) {

        Session session = em.unwrap(Session.class);
        ProcedureCall proc = session.createStoredProcedureCall("PROCESS_CUSTOM_RECORD_LIST");
        ParameterRegistration<ARRAY> registeredParameter = proc.registerParameter("p_list", ARRAY.class, ParameterMode.IN);
        proc.setParameter(registeredParameter, new TypedParameterValue(ListOfCustomUserType.INSTANCE, dtos));

        proc.execute();
//
//
//
//        //----------------------previous-------------------------------------
////        StoredProcedureQuery storedProcedure =
////                em.createStoredProcedureQuery("ProcessCustTestList");
////
////        // Register the custom type parameter
////        String paramName = "p_list";     //this parameter name must match param name in db.
////        storedProcedure.registerStoredProcedureParameter(paramName, ARRAY.class, ParameterMode.IN);
////
////        // Set the custom type parameter value
////        storedProcedure.setParameter(paramName, new TypedParameterValue(ListOfCustomUserType.INSTANCE, dtos ));
////
////        // Execute the stored procedure
////        storedProcedure.execute();
    }

}
