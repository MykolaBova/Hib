OLD SimpleORM 2.x example of useing Simpleorm with an EJB.

The idea was to associate the connection with the JTA transaction object.

The SConnection model has changed, and the subtype will not work without substantial changes.  It is also largely unnecessary with the new model.

Left here if anyone is interested in doing cleverer things with EJBs and JTA. 

