package cn.cerc.jdb.redis;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cerc.jdb.core.IHandle;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 该类主要是做 redis 链接测试 以及所有的数据查询新增
 * 
 * @author 欧阳香
 *
 */
public class RedisTool {

    private static Logger logger = LoggerFactory.getLogger("RedisSimpleTempalte");

    public static RedisSession sess;
    public static JedisPool jedisPool;

    public RedisTool(IHandle handle) {
        sess = (RedisSession) handle.getProperty(RedisSession.sessionId);
        jedisPool = sess.getJedisPool();
    }

    // index代表 缓存存放到哪一个db实例里面
    protected <T> T execute(RedisCallback<T> callback, Object... args) {
        Jedis jedis = null;
        try {
            Object index = ((Object[]) args)[0];
            System.err.println("-------------------index ------------" + index);
            if (null != index && Integer.parseInt(index.toString()) > 0 && Integer.parseInt(index.toString()) < 16) {
                jedis = sess.getRedis(Integer.parseInt(index.toString()));
            } else {
                jedis = sess.getRedis();
            }
            return callback.call(jedis, args);
        } catch (JedisConnectionException e) {
            if (jedis != null)
                sess.returnBrokeRedis(jedis);
            jedis = sess.getRedis();
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (jedis != null) {
                sess.returnRedis(jedis);
            }
        }
        return null;
    }

    /**
     * 选择DB实例
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void select(int index) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                int index = Integer.parseInt(((Object[]) parms)[0].toString());
                return jedis.select(index);
            }
        }, index);
    }

    /**
     * redis是否连接成功
     * 
     * @author ouyangxiang
     */
    public boolean isConnected(int index) {
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis, Object params) {
                return jedis.isConnected();
            }
        }, index);
    }

    /**
     * 是否存在key
     * 
     * @author ouyangxiang
     */
    public Boolean exists(int index, String key) {
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis, Object params) {
                String key = ((Object[]) params)[1].toString();
                return jedis.exists(key);
            }
        }, index, key);
    }

    /**
     * hash是否存在某个属性
     * 
     * @author ouyangxiang
     */
    public Boolean hexists(int index, String mapKey, String attributeKey) {
        return execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean call(Jedis jedis, Object params) {
                String key = ((Object[]) params)[1].toString();
                String field = ((Object[]) params)[2].toString();
                return jedis.hexists(key, field);
            }
        }, index, mapKey, attributeKey);
    }

    /**
     * 获取Hashmap（哈希）某个键值
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public String hget(int index, String key, String field) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String field = ((Object[]) parms)[2].toString();
                return jedis.hget(key, field);
            }
        }, index, key, field);
    }

    /**
     * 获取Hashmap（哈希）某个map
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public Map<String, String> hgetAll(int index, String key) {
        return execute(new RedisCallback<Map<String, String>>() {
            @Override
            public Map<String, String> call(Jedis jedis, Object params) {
                String key = ((Object[]) params)[1].toString();
                return jedis.hgetAll(key);
            }
        }, index, key);
    }

    /**
     * 删除Hashmap（哈希）某个map里面 某个键
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public Long hdel(int index, String mapKey, String attributeKey) {
        return execute(new RedisCallback<Long>() {
            @Override
            public Long call(Jedis jedis, Object params) {
                String key = ((Object[]) params)[1].toString();
                String field = ((Object[]) params)[2].toString();
                return jedis.hdel(key, field);
            }
        }, index, mapKey, attributeKey);
    }

    /**
     * Hash（哈希）
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void hset(int index, String key, String field, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String field = ((Object[]) parms)[2].toString();
                String value = ((Object[]) parms)[3].toString();
                jedis.hset(key, field, value);
                return null;
            }
        }, key, field, value);
    }

    /**
     * String（字符串）获取
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public String get(int index, String key) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                return jedis.get(key);
            }
        }, index, key);
    }

    /**
     * String（字符串）获取
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public byte[] getByte(int index, String key) {
        return execute(new RedisCallback<byte[]>() {
            public byte[] call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                try {
                    return jedis.get(key.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        }, index, key);
    }

    /**
     * String（字符串）赋值
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void set(int index, String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String value = ((Object[]) parms)[2].toString();
                jedis.set(key, value);
                return null;
            }
        }, index, key, value);
    }

    /**
     * 存储字符串 设置编码
     * 
     * @author ouyangxiang
     */
    public void set(int index, String key, byte[] value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                byte[] value = (byte[]) ((Object[]) parms)[2];
                try {
                    jedis.set(key.getBytes("UTF-8"), value);
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        }, index, key, value);
    }

    /**
     * seconds:过期时间（单位：秒）
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void set(int index, String key, String value, int seconds) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String value = ((Object[]) parms)[2].toString();
                String seconds = ((Object[]) parms)[3].toString();
                jedis.setex(key, Integer.parseInt(seconds), value);
                return null;
            }
        }, index, key, value, seconds);
    }

    /**
     * 设置db实例过期时间
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void set(int index, String key, byte[] value, int seconds) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                byte[] value = (byte[]) ((Object[]) parms)[2];
                String seconds = ((Object[]) parms)[3].toString();
                try {
                    jedis.setex(key.getBytes("UTF-8"), Integer.parseInt(seconds), value);
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        }, index, key, value, seconds);
    }

    /**
     * 批量Set
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void setPipeLine(int index, List<RedisKVPO> list) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                Pipeline p = jedis.pipelined();
                @SuppressWarnings("unchecked")
                List<RedisKVPO> values = (List<RedisKVPO>) ((Object[]) parms)[1];
                for (RedisKVPO po : values) {
                    p.set(po.getK(), po.getV());
                }
                p.sync();
                return null;
            }
        }, index, list);
    }

    /**
     * 根据key删除
     * 
     * @author ouyangxiang Date: 2017-08-18
     */
    public void del(int index, String key) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                jedis.del(key);
                return null;
            }
        }, index, key);
    }

    /**
     * Redis Llen 命令用于返回列表的长度。 如果列表 key 不存在， 则 key 被解释为一个空列表，返回 0 。 如果 key
     * 不是列表类型，返回一个错误。
     * 
     * @author ouyangxiang
     */
    public String llen(int index, String key) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                return jedis.llen(key) + "";
            }
        }, index, key);
    }

    /**
     * list 新增
     * 
     * @author ouyangxiang
     */
    public void lpush(int index, String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String value = ((Object[]) parms)[2].toString();
                jedis.lpush(key, value);
                return null;
            }
        }, index, key, value);
    }

    /**
     * list 新增
     * 
     * @author ouyangxiang
     */
    public void lpushPipeLine(int index, String key, List<String> values) {
        execute(new RedisCallback<String>() {
            @SuppressWarnings("unchecked")
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                List<String> values = (List<String>) ((Object[]) parms)[2];
                Pipeline p = jedis.pipelined();
                for (String value : values) {
                    p.lpush(key, value);
                }
                p.sync();// 同步
                return null;
            }
        }, index, key, values);
    }

    /**
     * jedis操作List 查询
     * 
     * @author ouyangxiang
     */
    public List<String> lrange(int index, String key, long start, long end) {
        return execute(new RedisCallback<List<String>>() {
            public List<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[1].toString();
                long start = Long.parseLong(ps[2].toString());
                long end = Long.parseLong(ps[3].toString());
                return jedis.lrange(key, start, end);
            }
        }, index, key, start, end);
    }

    /**
     * int类型 //进行加1操作
     * 
     * @author ouyangxiang
     */
    public void incr(int index, String key) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                jedis.incr(key);
                return null;
            }
        }, index, key);
    }

    /**
     * jedis操作Set
     * 
     * @author ouyangxiang
     */
    public void sadd(int index, String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[1].toString();
                String value = ((Object[]) parms)[2].toString();
                jedis.sadd(key, value);
                return null;
            }
        }, index, key, value);
    }

    /**
     * 返回key集合所有的元素.
     * 
     * @author ouyangxiang
     */
    public Set<String> smembers(int index, String key) {
        return execute(new RedisCallback<Set<String>>() {
            public Set<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[1].toString();
                return jedis.smembers(key);
            }
        }, index, key);
    }

    /**
     * Redis Brpop 命令移出并获取列表的最后一个元素(list)
     * 
     * @author ouyangxiang
     */
    public List<String> brpop(int index, String key) {
        return execute(new RedisCallback<List<String>>() {
            public List<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[1].toString();
                return jedis.brpop(0, key);
            }
        }, index, key);
    }

}

class RedisKVPO {
    private String key;
    private String value;

    public RedisKVPO() {
    }

    public RedisKVPO(String k, String v) {
        this.key = k;
        this.value = v;
    }

    public String getK() {
        return key;
    }

    public void setK(String k) {
        this.key = k;
    }

    public String getV() {
        return value;
    }

    public void setV(String v) {
        this.value = v;
    }

}

interface RedisCallback<T> {
    public T call(Jedis jedis, Object params);
}