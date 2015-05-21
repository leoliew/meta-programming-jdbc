package com.dao;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class BasicDao<T> {

    protected ResultSet rs;//返回的结果集
    protected PreparedStatement pstm;//预编译
    protected Connection conn;//建立连接
    private static DataSource ds=null;//MySql连接池
    //要执行操作的实体
    private Class clazz;
    //通过构造方法获取连接
    public BasicDao() {
        if (this.clazz == null) {
            // 取得传入的类型.也就是子类的<泛型>中的类型
            Class clazz = getClass();
            // 返回表示此 Class 所表示的实体（类、接口、基本类型或 void）的直接超类的 Type。
            // 如果超类是参数化类型(泛型)，则返回的 Type 对象必须准确反映源代码中所使用的实际类型参数<>
            // ParameterizedType参数化类型的类
            // com.jdbc.conn.BaseDao<com.jdbc.pojo.User>
            ParameterizedType type = (ParameterizedType) clazz.getGenericSuperclass();
            // type.getActualTypeArguments()返回表示此类型实际类型参数的 Type 对象的数组
            // 因为在泛型类的参数中可以有多个，如BaseDao<T,R>,HashMap<K, V>
            this.clazz = (Class) type.getActualTypeArguments()[0];
        }
    }

    
    /**
     * 初始化连接
     */
	public  Connection getConnection(){
    	try {
			conn= ds.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return conn;
    }
	
	
	/**
	 * 初始化数据
	 */
	public static void initData(){
		Context context; 	
			if(ds==null){
				try {
					context = new InitialContext();
					ds = (DataSource) context.lookup("java:comp/env/jdbc/oracle");//jdbc/bs为自己配置的jndi的名称
				} catch (NamingException e) {
					e.printStackTrace();
				}
			}		
	}
	
    
    /**
     * 非查询语句的操作
     * @param sql 非查询的sql语句
     * @param objects  非查询语句的参数
     * @return 受影响的行数
     */
    public int executeUpdatePerparement(String sql, Object...objects) {
        int row = 0;
        try {
        	conn=getConnection();
            //创建prepareStatement ,预编译sql语句
            pstm = conn.prepareStatement(sql);
            if (objects != null && objects.length > 0) {
                for (int i = 0; i < objects.length; i++) {
                    //使用给定对象设置指定参数的值。
                    pstm.setObject(i + 1, objects[i]);
                }
            }          
            //执行语句
            row = pstm.executeUpdate();
            close(conn, pstm, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    /**
     * 查询语句的操作
     * @param sql 查询语句
     * @param objects 查询语句的参数
     * @return 结果集
     */
    public ResultSet select(String sql, Object... objects) {
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
           if (objects != null && objects.length > 0) {
                for (int i = 0; i < objects.length; i++) {
                    pstm.setObject(i + 1, objects[i]);
                }
            }
            rs = pstm.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     *根据sql语句查找数据库中的记录
     * @param sql 要查找的Sql语句
     * @param objects 传入的参数
     * @return 返回的List集合
     */
    public List<T> findListT(String sql, Object... objects) {
        List<T> list = new ArrayList<T>();
        T vo = null;
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
            if (objects != null && objects.length > 0) {
                for (int i = 0; i < objects.length; i++) {
                    pstm.setObject(i + 1, objects[i]);
                }
            }
            rs = pstm.executeQuery();
            while (rs.next()) {
                vo = autoBeanByRs(rs);
                list.add(vo);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        close(conn, pstm, rs);
        return list;
    }

    /**
     *根据sql语句查找数据库中的记录进行模糊查询
     * @param sql 要查找的Sql语句
     * @param objects 传入的参数
     * @return 返回的List集合
     */
    public List<T> fuzzyListT(String sql, Object... objects) {
        List<T> list = new ArrayList<T>();
        T vo = null;
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
            if (objects != null && objects.length > 0) {
                for (int i = 0; i < objects.length; i++) {
                    //进行模糊查询
                    pstm.setObject(i + 1, "%" + objects[i] + "%");
                }
            }
            rs = pstm.executeQuery();
            while (rs.next()) {
                vo = autoBeanByRs(rs);
                list.add(vo);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        close(conn, pstm, rs);
        return list;
    }

    /**
     * 返回一个实体类
     * @param sql 查询的sql语句
     * @param t 传入的实体类
     * @param objects 查询语句的参数
     * @return 实体类
     */
    public T findByPredStatmentT(String sql, Object... objects) {
        T vo = null;
        try {
            conn = getConnection();
            pstm = conn.prepareStatement(sql);
            if (objects != null && objects.length > 0) {
                for (int i = 0; i < objects.length; i++) {
                    pstm.setObject(i + 1, objects[i]);
                }
            }
            rs = pstm.executeQuery();
            while (rs.next()) {
                vo = autoBeanByRs(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(conn, pstm, rs);
        return vo;
    }

    /**
     * 根据结果集返回实体Bean
     * @param rs 查询出来的结果集
     * @param t 要转换的实体Bean
     * @return 返回修改后的实体Bean
     */
    @SuppressWarnings("unchecked")
    private T autoBeanByRs(ResultSet rs) {
        //要返回的实体Bean
        T vo = null;
        try {
            //初始化实体Bean
            vo = (T) clazz.newInstance();
            //结果集中列的数量
            int columnCount = 0;
            ResultSetMetaData meteData = rs.getMetaData();
            columnCount = meteData.getColumnCount();
            //字段类型int ,varchar
            String className = null;
            //迭代结果集中的列
            for (int i = 1; i <= columnCount; i++) {
                className = meteData.getColumnClassName(i);
//System.out.println("columnType>>>>"+columnType);
                //实体Bean的类型和列名一致
                String name = meteData.getColumnName(i);
                //拼装成set方法
                String setterName = "set" + name.substring(0, 1).toUpperCase()
                        + name.substring(1);
                //通过反射取得方法名
                Method method = clazz.getDeclaredMethod(setterName,
                        Class.forName(className));
                method.invoke(vo, rs.getObject(name));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return vo;
    }

    /**
     * 根据条件查找信息
     * @param sql
     * @param t
     * @return 
     */
    public List findListTByCondition(String sql, T t) {
        List<T> list = new ArrayList<T>();
        T vo = null;
        StringBuffer bsql = new StringBuffer(sql);
        bsql.append(" 1=1 ");
        Class tclazz = t.getClass();
        Field[] fields = t.getClass().getDeclaredFields();//获得属性
         conn = getConnection();
        try {
            for (Field field : fields) {    
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(),
                        tclazz);
                //通过反射获取get方法的属性
                Method getMethod = pd.getReadMethod();//执行get方法返回一个Object
                Object o = getMethod.invoke(t);//执行get方法返回一个Object
                if (o != null) {
                    //生成根据信息查询的sql语句
                    bsql.append(" and ").append(field.getName());
                    bsql.append("='").append(o).append("'");
                }
            }
            System.out.println(bsql);
            pstm = conn.prepareStatement(bsql.toString());
            rs = pstm.executeQuery();
            //根据结果集返回集合
            while (rs.next()) {
                vo = autoBeanByRs(rs);
                list.add(vo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(BasicDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BasicDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BasicDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(BasicDao.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IntrospectionException ex) {
            Logger.getLogger(BasicDao.class.getName()).log(Level.SEVERE, null, ex);
        }
        close(conn, pstm, rs);
        return list;
    }
    
    // 关闭资源
    public static void close(Connection conn, PreparedStatement pstm,
            ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            if (conn != null && conn.getAutoCommit() == true) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(" 关闭JDBC 资源 出错 ");
            e.printStackTrace();
        }

    }
}
