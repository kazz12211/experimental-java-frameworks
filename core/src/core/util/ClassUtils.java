package core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import ariba.util.core.GrowOnlyHashtable;
import ariba.util.core.NonCachingClassLoader;

public class ClassUtils {

    public static String NativeInteger = "int";
    public static String NativeBoolean = "boolean";
    public static String NativeDouble = "double";
    public static String NativeFloat = "float";
    public static String NativeLong = "long";
    public static String NativeByte = "byte";
    public static String NativeShort = "short";
    public static String NativeChar = "char";

    private ClassUtils () {}
    
    public static void classTouch (String name)
    {
       classForName(name, Object.class, false);
    }

    public static Class classForNativeType (String typeName)
    {
        if (NativeInteger.equals(typeName)) {
            return Integer.TYPE;
        }
        if (NativeBoolean.equals(typeName)) {
            return Boolean.TYPE;
        }
        if (NativeDouble.equals(typeName)) {
            return Double.TYPE;
        }
        if (NativeFloat.equals(typeName)) {
            return Float.TYPE;
        }
        if (NativeLong.equals(typeName)) {
            return Long.TYPE;
        }
        if (NativeByte.equals(typeName)) {
            return Byte.TYPE;
        }
        if (NativeShort.equals(typeName)) {
            return Short.TYPE;
        }
        if (NativeChar.equals(typeName)) {
            return Character.TYPE;
        }
        return null;            
    }

    public static Class classForName (String className)
    {
        return classForName(className, Object.class, true);
    }

    public static Class classForName (String className,
                                      Class  supposedSuperclass)
    {
        return classForName(className, supposedSuperclass, true);
    }

    private static final GrowOnlyHashtable ClassForNameCache =
        new GrowOnlyHashtable();

    private static final Object NoCachedClassFound = Constants.NullObject;

    private static final GrowOnlyHashtable LocaleCache =
        new GrowOnlyHashtable();

    private static ClassFactory classFactory = null;


    public static ClassFactory setClassFactory (ClassFactory cf)
    {
        ClassFactory oldcf = classFactory;
        classFactory = cf;
        return oldcf;
    }
    
    public static ClassFactory getClassFactory ()
    {
        return classFactory;
    }
    
    public static Class classForName (String className,
                                      boolean warning)
    {
        return classForName(className, Object.class, warning);
    }

    public static Class classForName (String className,
                                      Class supposedSuperclass,
                                      boolean warning)
    {
        if (classFactory != null) {
            return classFactory.forName(className);
        }
        
        if (className == null) {
            return null;
        }

        if (useContextClassLoader) {
            return classForNameUsingContextClassLoader(className,
                                                       supposedSuperclass,
                                                       warning);
        }

        int leftGenericIndex = className.indexOf('<');
        if (leftGenericIndex > 0) {
            if (className.charAt(className.length() - 1) == '>') {
                className = className.substring(0, leftGenericIndex);
            }
            else {
                Log.coreUtil.debug("Getting malformed Generics className: " + className);
            }
        }

        Object cachedClass = ClassForNameCache.get(className);
        if (cachedClass == NoCachedClassFound) {
            if (warning) {
                Log.coreUtil.error(2764, className);
            }
            return null;
        }
        if (cachedClass != null) {
            return checkInstanceOf((Class)cachedClass,
                                   supposedSuperclass,
                                   warning);
        }

        try {
            Class classObj = Class.forName(className);
            ClassForNameCache.put(className, classObj);
            return checkInstanceOf(classObj, supposedSuperclass, warning);
        }
        catch (ClassNotFoundException e) {
            if (Log.coreUtil.isDebugEnabled()) {
                Log.coreUtil.debug("classForName: " + SystemUtils.stackTrace(e));
            }
            ClassForNameCache.put(className, NoCachedClassFound);
            return classForName(className, warning);
        }
        catch (NoClassDefFoundError e) {
            if (Log.coreUtil.isDebugEnabled()) {
                Log.coreUtil.debug("classForName: " + SystemUtils.stackTrace(e));
            }
            ClassForNameCache.put(className, NoCachedClassFound);
            return classForName(className, warning);
        }
        catch (SecurityException e) {
            if (Log.coreUtil.isDebugEnabled()) {
                Log.coreUtil.debug("classForName: " + SystemUtils.stackTrace(e));
            }
            ClassForNameCache.put(className, NoCachedClassFound);
            return classForName(className, warning);
        }    
    }
    
    public static Class classForNameWithException (String className)
      throws ClassNotFoundException
    {
        Class returnVal = classForName(className, Object.class, false);
        if (returnVal == null) {
            throw new ClassNotFoundException("Could not find class " + className);
        }
        return returnVal;
    }

    public static Class classForNameNonCaching (String className,
                                                String pattern,
                                                boolean warning)
    {
        Assert.that(className != null, "className should not be null");

        try {
            NonCachingClassLoader nccl = new NonCachingClassLoader(pattern);
            return nccl.loadClass(className);
        }
        catch (ClassNotFoundException e) {
        }
        catch (NoClassDefFoundError e) {
        }
        catch (SecurityException e) {
        }
        if (warning) {
            Log.coreUtil.error(2764, className);
        }
        return null;
    }

    public static boolean instanceOf (Object object, String className)
    {
        if (object == null) {
            return false;
        }
        return instanceOf(object.getClass(), classForName(className));
    }

    public static boolean instanceOf (Class instance, Class target)
    {
        if (target == null) {
            return false;
        }
        return target.isAssignableFrom(instance);
    }

    public static String getClassNameOfObject (Object o)
    {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName();
    }

    public static Object newInstance (String className)
    {
        return newInstance(className, true);
    }

    public static Object newInstance (String className, boolean warning)
    {
        return newInstance(classForName(className, warning));
    }

    public static Object newInstance (String className,
                                      String supposedSuperclassName)
    {
        return newInstance(className, supposedSuperclassName, true);
    }

    public static Object newInstance (String className,
                                      String supposedSuperclassName,
                                      boolean warning)
    {
        return newInstance(className,
                           classForName(supposedSuperclassName, warning),
                           warning);
    }
    public static Object newInstance (String  className,
                                      Class   supposedSuperclass,
                                      boolean warning)
    {
        return newInstance(classForName(className, warning),
                           supposedSuperclass,
                           warning);
    }

    public static Object newInstance (Class   classObj,
                                      Class   supposedSuperclass,
                                      boolean warning)
    {
        if (classObj == null) {
            return null;
        }
        if (supposedSuperclass == null) {
            return null;
        }
        Class clazz = checkInstanceOf(classObj, supposedSuperclass, warning);
        if (clazz == null) {
            return null;
        }
        return newInstance(clazz);
    }

    public static Object newInstance (Class theClass)
    {
        if (theClass == null) {
            return null;
        }
        try {
            return theClass.newInstance();
        }
        catch (InstantiationException e) {
            Log.coreUtil.error(2765, theClass.getName(), e);
        }
        catch (IllegalAccessException e) {
            Log.coreUtil.error(2766, theClass.getName(), e);
        }
        return null;
    }

    public static String stripPackageFromClassName (String className)
    {
        int pos = className.lastIndexOf('.');
        if (pos > 0) {
            return className.substring(pos + 1);
        }
        return className;
    }

    public static String stripClassFromClassName (String className)
    {
        int pos = className.lastIndexOf('.');
        if (pos > 0) {
            return className.substring(0, pos);
        }
        return "";
    }

    public static Object invokeStaticMethod (String   className,
                                             String   methodName)
    {
        try {
            Class c = ClassUtils.classForName(className);
            if (c != null) {
                Method m = c.getMethod(methodName);
                return m.invoke(null);
            }
        }
        catch (NoSuchMethodException e) {
        }
        catch (InvocationTargetException e) {
        }
        catch (IllegalAccessException e) {
        }
        return null;
    }

    public static Object invokeStaticMethod (String   className,
                                             String   methodName,
                                             Class[]  paramTypes,
                                             Object[] args)
    {
        try {
            Class c = ClassUtils.classForName(className);
            if (c != null) {
                Method m = c.getMethod(methodName, paramTypes);
                return m.invoke(null, args);
            }
        }
        catch (NoSuchMethodException e) {
            Assert.that(false, "NoSuchMethod :" + SystemUtils.stackTrace(e));
        }
        catch (InvocationTargetException e) {
            Assert.that(false, "InvocationTargetException :" + SystemUtils.stackTrace(e));
        }
        catch (IllegalAccessException e) {
            Assert.that(false, "IllegalAccessException :" + SystemUtils.stackTrace(e));
        }
        return null;
    }

    public static Field[] getDeclaredFields (Class clazz)
    {
            // Also count fields to allocate array size
        int fieldCount = 0;
        Class c = clazz;
        while (c != null) {
            fieldCount += c.getDeclaredFields().length;
            c = c.getSuperclass();
        }
        Field[] fields = new Field[fieldCount];

        c = clazz;
        while (c != null) {
            Field[] declaredFields = c.getDeclaredFields();
            int length = declaredFields.length;
            fieldCount -= length;
            System.arraycopy(declaredFields, 0,
                             fields, fieldCount, 
                             length);
            c = c.getSuperclass();
        }
        return fields;
    }

    private static final Map typeToVMMap = MapUtils.map();
    static {
        typeToVMMap.put(Constants.CharPrimitiveType,
                        Constants.JavaCharAbbreviation);
        typeToVMMap.put(Constants.BytePrimitiveType,
                        Constants.JavaByteAbbreviation);
        typeToVMMap.put(Constants.ShortPrimitiveType,
                        Constants.JavaShortAbbreviation);
        typeToVMMap.put(Constants.IntPrimitiveType,
                        Constants.JavaIntAbbreviation);
        typeToVMMap.put(Constants.LongPrimitiveType,
                        Constants.JavaLongAbbreviation);
        typeToVMMap.put(Constants.FloatPrimitiveType,
                        Constants.JavaFloatAbbreviation);
        typeToVMMap.put(Constants.DoublePrimitiveType,
                        Constants.JavaDoubleAbbreviation);
        typeToVMMap.put(Constants.BooleanPrimitiveType,
                        Constants.JavaBooleanAbbreviation);
    }

    public static String typeToVMType (String type)
    {
        String abbrev = (String)typeToVMMap.get(type);
        if (abbrev != null) {
            return abbrev;
        }
        return "L" +  type + ";";
    }

    private static Class classForNameUsingContextClassLoader (
        String  className,
        Class   supposedSuperclass,
        boolean warning)
    {
        try {
            ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
            return checkInstanceOf(Class.forName(className, true, loader), // OK
                                   supposedSuperclass,
                                   warning);
        }
        catch (ClassNotFoundException e) {
                // dealt with below
        }
        catch(NoClassDefFoundError e) {
                // dealt with below
        }
        catch (SecurityException e) {
                // dealt with below
        }
        if (warning) {
            Log.coreUtil.error(2764, className);
        }
        return null;
    }

    private static Class checkInstanceOf (Class classObj,
                                          Class supposedSuperclass,
                                          boolean warning)
    {
        if (instanceOf(classObj, supposedSuperclass)) {
            return classObj;
        }
        if (warning) {
            Log.coreUtil.error(4803, classObj.getName(), supposedSuperclass.getName());
        }
        return null;
    }

    private static final String UseContextClassLoaderProperty =
        "core.util.ClassUtils.useContextClassLoader";

    private static final boolean useContextClassLoader;

    static {
        boolean b = Boolean.getBoolean(UseContextClassLoaderProperty);
        useContextClassLoader = b;
    }

    private static final Class[] cloneArgType = new Class[0];
    private static final Object[] cloneArgValues = new Object[0];

    static Object clone (Object o)
    {
        Assert.that(o instanceof Cloneable, "Object is not cloneable: " + o.getClass().getName());
        try {
            Class thisClass = o.getClass();
            Method m = thisClass.getMethod("clone", cloneArgType);
            return m.invoke(o, cloneArgValues);
        }
        catch (NoSuchMethodException e) {
            Assert.that(false, "NoSuchMethod :" + SystemUtils.stackTrace(e));
        }
        catch (InvocationTargetException e) {
            Assert.that(false, "InvocationTargetException :" + SystemUtils.stackTrace(e));
        }
        catch (IllegalAccessException e) {
            Assert.that(false, "IllegalAccessException :" + SystemUtils.stackTrace(e));
        }
        return null;
    }
}
