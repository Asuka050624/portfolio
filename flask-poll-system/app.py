#!/usr/bin/env python3
""" Basic Poll Example for Fontys SOT Course

This example is the basis for your exploration in Service Oriented Technology. We will make many changes.

Changes:
1.0.0: This version uses HTTP GET and will reply with XML responses.
2.0.0: This version uses XML-RPC and will reply with XML-RPC responses, but cannot yet vote or get results.
2.1.0: This version adds vote and get_result to the API.
3.0.0: This version switches to a RESTful API.
3.1.0: This version refactors the RESTful API with flask_restful.
3.2.0: This version makes the process stateless with a Redis backend.
"""
import logging  # 导入日志模块，用于记录应用运行状态
import os  # 导入操作系统模块，用于获取环境变量
from flask import Flask  # 导入Flask主类，用于创建Web应用
from flask import abort  # 导入abort函数，用于返回HTTP错误状态
from flask import jsonify  # 导入jsonify函数，用于返回JSON格式响应
from flask import render_template  # 导入render_template函数，用于渲染HTML模板
from flask_restful import Api, Resource, reqparse  # 导入Flask-RESTful组件
import redis  # 导入Redis客户端库，用于连接Redis数据库

__AUTHOR__ = "Caixia & Roy Berkeveld"  # 作者信息
__VERSION__ = "3.2.0"  # 应用版本号
__MAJOR__ = __VERSION__.split(".")[0]  # 主版本号，用于API路由

logging.basicConfig(level=logging.INFO)  # 配置日志级别为INFO

app = Flask(__name__)  # 创建Flask应用实例
api = Api(app)  # 创建API实例，用于注册RESTful资源
redis_host = os.environ.get('REDIS_HOST', 'localhost')  # 从环境变量获取Redis主机地址，默认为localhost
redis_client = redis.StrictRedis(host=redis_host, port=6379, db=0, decode_responses=True)  # 创建Redis客户端连接


def abort_if_poll_doesnt_exist(poll_id):
    """
    检查投票是否存在的辅助函数
    如果投票不存在，则返回404错误
    
    参数:
        poll_id: 投票的ID
    """
    if not redis_client.exists(f'poll:{poll_id}'):  # 检查Redis中是否存在该投票
        abort(404, "Poll {} doesn't exist".format(poll_id))  # 返回404错误


class Poll(Resource):
    """
    投票资源类，实现投票的CRUD操作
    """
    
    def get(self, poll_id=None):
        """
        获取投票信息
        如果提供了poll_id，则获取单个投票的信息
        如果没有提供poll_id，则获取所有投票的ID列表
        
        参数:
            poll_id: 投票的ID（可选）
            
        返回:
            单个投票信息或所有投票ID列表
        """
        if poll_id:  # 如果提供了poll_id
            abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
            poll_info = redis_client.hgetall(f'poll:{poll_id}')  # 从Redis获取投票信息
            return {'poll_id': poll_id, 'question': poll_info['question']}  # 返回投票信息
        else:  # 如果没有提供poll_id
            polls = redis_client.keys('poll:*')  # 获取所有投票的键
            return {'polls': [int(poll.split(':')[1]) for poll in polls]}  # 返回投票ID列表

    def post(self):
        """
        创建新投票
        
        返回:
            新创建投票的ID和201状态码
        """
        parser = reqparse.RequestParser()  # 创建请求参数解析器
        parser.add_argument('question', required=True, help="Question cannot be blank")  # 添加必填参数question
        args = parser.parse_args()  # 解析请求参数
        poll_id = redis_client.incr('next_poll_id')  # 生成新的投票ID（自增）
        redis_client.hset(f'poll:{poll_id}', mapping={'question': args['question']})  # 将投票信息存储到Redis
        return {'poll_id': poll_id}, 201  # 返回新投票ID和201状态码

    def put(self, poll_id):
        """
        更新投票信息
        
        参数:
            poll_id: 投票的ID
            
        返回:
            更新后的投票信息和200状态码
        """
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        parser = reqparse.RequestParser()  # 创建请求参数解析器
        parser.add_argument('question', required=True, help="Question cannot be blank")  # 添加必填参数question
        args = parser.parse_args()  # 解析请求参数
        redis_client.hset(f'poll:{poll_id}', 'question', args['question'])  # 更新Redis中的投票信息
        return {'poll_id': poll_id, 'question': args['question']}, 200  # 返回更新后的投票信息

    def delete(self, poll_id):
        """
        删除投票
        
        参数:
            poll_id: 投票的ID
            
        返回:
            空内容和204状态码
        """
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        redis_client.delete(f'poll:{poll_id}')  # 从Redis删除投票
        return '', 204  # 返回空内容和204状态码


class PollOption(Resource):
    """
    投票选项资源类，实现投票选项的CRUD操作
    """
    
    def get(self, poll_id, option_id=None):
        """
        获取投票选项信息
        如果提供了option_id，则获取单个选项的信息
        如果没有提供option_id，则获取所有选项的ID列表
        
        参数:
            poll_id: 投票的ID
            option_id: 选项的ID（可选）
            
        返回:
            单个选项信息或所有选项ID列表
        """
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        if option_id:  # 如果提供了option_id
            option_description = redis_client.hget(f'option:{poll_id}', str(option_id))  # 从Redis获取选项描述
            if not option_description:  # 如果选项不存在
                abort(404, "Option {} doesn't exist in Poll {}".format(option_id, poll_id))  # 返回404错误
            return {'option_id': option_id, 'description': option_description}  # 返回选项信息
        else:  # 如果没有提供option_id
            options = redis_client.hgetall(f'option:{poll_id}')  # 获取该投票的所有选项
            return {'options': [int(opt_id) for opt_id in options.keys()]}  # 返回选项ID列表

    def post(self, poll_id):
        """
        为投票添加新选项
        
        参数:
            poll_id: 投票的ID
            
        返回:
            新创建选项的信息和201状态码
        """
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        parser = reqparse.RequestParser()  # 创建请求参数解析器
        parser.add_argument('description', required=True, help="Description cannot be blank")  # 添加必填参数description
        args = parser.parse_args()  # 解析请求参数
        option_id = redis_client.incr(f'next_option_id:{poll_id}')  # 生成新的选项ID（自增）
        redis_client.hset(f'option:{poll_id}', option_id, args['description'])  # 将选项信息存储到Redis
        return {'poll_id': poll_id, 'option_id': option_id, 'description': args['description']}, 201  # 返回新选项信息

    def put(self, poll_id, option_id):
        """
        更新投票选项信息
        
        参数:
            poll_id: 投票的ID
            option_id: 选项的ID
            
        返回:
            更新后的选项信息
        """
        parser = reqparse.RequestParser()  # 创建请求参数解析器
        parser.add_argument('description', required=True, help="Description cannot be blank")  # 添加必填参数description
        args = parser.parse_args()  # 解析请求参数
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        redis_client.hset(f'option:{poll_id}', option_id, args['description'])  # 更新Redis中的选项信息
        return {'poll_id': poll_id, 'option_id': option_id, 'description': args['description']}  # 返回更新后的选项信息

    def delete(self, poll_id, option_id):
        """
        删除投票选项
        
        参数:
            poll_id: 投票的ID
            option_id: 选项的ID
            
        返回:
            空内容和204状态码
        """
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        if not redis_client.hexists(f'option:{poll_id}', str(option_id)):  # 检查选项是否存在
            abort(404, "Option {} doesn't exist in Poll {}".format(option_id, poll_id))  # 返回404错误
        redis_client.hdel(f'option:{poll_id}', option_id)  # 从Redis删除选项
        return '', 204  # 返回空内容和204状态码


class Vote(Resource):
    """
    投票资源类，实现提交投票功能
    """
    
    def put(self, poll_id, voter_id):
        """
        提交投票
        防止重复投票
        
        参数:
            poll_id: 投票的ID
            voter_id: 投票者的ID
            
        返回:
            投票结果
        """
        parser = reqparse.RequestParser()  # 创建请求参数解析器
        parser.add_argument('option_id', required=True, help="Option ID cannot be blank")  # 添加必填参数option_id
        args = parser.parse_args()  # 解析请求参数
        abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
        if not redis_client.sismember(f'voters:{poll_id}', voter_id):  # 检查用户是否已经投票
            redis_client.sadd(f'voters:{poll_id}', voter_id)  # 将用户添加到已投票集合中
            redis_client.incr(f'votes:{poll_id}:{args["option_id"]}')  # 该选项的票数加1
            return {'voter_id': voter_id, 'voted': True}  # 返回投票成功
        else:  # 如果用户已经投票
            abort(400, "Voter has already voted")  # 返回400错误


def init_db():
    """
    初始化数据库
    如果数据库为空，则创建一个默认的投票
    """
    if not redis_client.exists('next_poll_id'):  # 如果next_poll_id不存在
        redis_client.set('next_poll_id', 0)  # 初始化next_poll_id为0

    if not redis_client.keys('poll:*'):  # 如果没有任何投票
        poll_id = redis_client.incr('next_poll_id')  # 生成投票ID
        poll_data = {
            'question': "What is your CPU Architecture?"
        }
        redis_client.hset(f'poll:{poll_id}', mapping=poll_data)  # 存储投票信息
        options = ['X86 (32-bit OS)', 'AMD64 (64-bit PC/Intel Mac)', 'ARM64 (Apple Silicon)']  # 默认选项
        for option in options:  # 遍历选项
            option_id = redis_client.incr(f'next_option_id:{poll_id}')  # 生成选项ID
            redis_client.hset(f'option:{poll_id}', option_id, option)  # 存储选项信息
        logging.info("Initialized database with a default poll and options.")  # 记录日志


# 注册Poll资源到API路由
api.add_resource(Poll,
                 f'/v{__MAJOR__}/polls/',  # 获取所有投票或创建新投票
                 f'/v{__MAJOR__}/polls/<int:poll_id>/')  # 获取、更新或删除单个投票
# 注册PollOption资源到API路由
api.add_resource(PollOption,
                 f'/v{__MAJOR__}/polls/<int:poll_id>/options/',  # 获取所有选项或添加新选项
                 f'/v{__MAJOR__}/polls/<int:poll_id>/options/<int:option_id>/')  # 获取、更新或删除单个选项
# 注册Vote资源到API路由
api.add_resource(Vote,
                 f'/v{__MAJOR__}/polls/<int:poll_id>/voters/<string:voter_id>/')  # 提交投票


@app.route(f"/v{__MAJOR__}/", methods=['GET'])
def index():
    """
    首页路由，返回所有可用路由列表
    """
    return jsonify({"routes": [x.rule for x in app.url_map.iter_rules()]})


@app.route(f'/v{__MAJOR__}/polls/<int:poll_id>/result', methods=['GET'])
def get_poll_results(poll_id):
    """
    获取投票结果
    计算每个选项的得票数和百分比
    
    参数:
        poll_id: 投票的ID
        
    返回:
        投票结果信息
    """
    abort_if_poll_doesnt_exist(poll_id)  # 检查投票是否存在
    options = redis_client.hgetall(f'option:{poll_id}')  # 获取所有选项
    results = []  # 结果列表
    total_votes = 0  # 总票数
    for option_id in options.keys():  # 遍历每个选项
        votes = int(redis_client.get(f'votes:{poll_id}:{option_id}') or 0)  # 获取该选项的票数
        total_votes += votes  # 累加总票数
        results.append({  # 添加结果到列表
            'option_id': int(option_id),
            'description': options[option_id],
            'votes': votes,
            'percentage': round(votes / total_votes * 100, 2) if total_votes > 0 else 0
        })
    return jsonify({  # 返回投票结果
        'poll_id': poll_id,
        'question': redis_client.hget(f'poll:{poll_id}', 'question'),
        'total_votes': total_votes,
        'results': results
    })


@app.route('/')
def home():
    """
    首页路由，渲染投票系统界面
    """
    return render_template('index.html')


@app.route('/api-test')
def api_test():
    """
    API测试工具路由
    """
    return render_template('api_test.html')


def run_server():
    """
    运行Flask服务器
    """
    app.run()


if __name__ == '__main__':  # 如果作为主程序运行
    init_db()  # 初始化数据库
    app.run()  # 启动Flask应用