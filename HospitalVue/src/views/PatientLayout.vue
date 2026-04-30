<template>
    <div>
        <!-- <div class="indexImage">
        <img src="@/assets/hospital.jpeg" class="layoutImage"/>
        <span>今天预约挂号总人数：{{orderPeople}}</span>
      </div> -->
      <div class="indexPeople" style="margin-left: 350px">
        <div class="userImage">
          <i class="el-icon-user" style="font-size: 132px"></i>
        </div>

        <div class="userFont">
          <div class="spanCure">
            <span>就诊概况</span>
          </div>
          <div class="spanPeople">
            <span>今天预约挂号总人数：{{ orderPeople }}</span>
          </div>
        </div>
      </div>
        <div class="indexPeople">
            <div class="userImage">
                <i class="el-icon-office-building" style="font-size: 132px"></i>
            </div>

            <div class="userFont">
                <div class="spanCure">
                    <span>住院概况</span>
                </div>
                <div class="spanPeople">
                    <span>今天住院总人数：{{ bedPeople }}</span>
                </div>
            </div>
        </div>

        <el-row>
            <el-col :span="24">
                <img src="@/assets/16.png" style="width: 641px;margin-left: 490px;">
            </el-col>
        </el-row>
        <div class="ai-assistant">
    <div class="chat-window" v-if="showChat">
        <div class="chat-header">
            <span><i class="el-icon-monitor"></i> 医疗智能助手</span>
            <i class="el-icon-close" @click="showChat = false" style="cursor:pointer"></i>
        </div>
        <div class="chat-body" ref="messageBox">
            <div v-for="(msg, index) in messages" :key="index" :class="['msg-item', msg.role]">
                <div class="msg-content">{{ msg.content }}</div>
            </div>
            <div v-if="loading" class="msg-item bot">
                <div class="msg-content"><i class="el-icon-loading"></i> 正在分析症状...</div>
            </div>
        </div>
        <div class="chat-footer">
            <el-input 
                size="small" 
                v-model="inputMsg" 
                placeholder="描述您的症状..." 
                @keyup.enter.native="sendToAI">
                <el-button slot="append" icon="el-icon-send" @click="sendToAI" :disabled="loading">发送</el-button>
            </el-input>
        </div>
    </div>
    <el-button 
        v-if="!showChat" 
        type="primary" 
        circle 
        class="ai-btn" 
        icon="el-icon-chat-dot-round" 
        @click="showChat = true">
    </el-button>
</div>
    </div> 
</template>
<script>
import request from "@/utils/request.js";
export default {
    name: "PatientLayout",
    data() {
        return {
            orderPeople: 1,
            bedPeople: 1,
            // 在 data() 中增加
            showChat: false,
            inputMsg: '',
            loading: false,
            messages: [
                { role: 'bot', content: '您好，我是您的医疗助手。请问有什么可以帮您？（本建议仅供参考，危急情况请立即拨打120）' }
            ],
        };
    },
    methods: {
        requestPeople() {
            request
                .get("order/orderPeople")
                .then((res) => {
                    if (res.data.status !== 200)
                        return this.$message.error("数据请求失败");
                    this.orderPeople = res.data.data;
                })
                .catch((err) => {
                    console.error(err);
                });
        },
        requestBed() {
            request
                .get("bed/bedPeople")
                .then((res) => {
                    if (res.data.status !== 200)
                        return this.$message.error("数据请求失败");
                    this.bedPeople = res.data.data;
                })
                .catch((err) => {
                    console.error(err);
                });
        },
        // 在 methods 中增加
  async sendToAI() {
    if (!this.inputMsg || this.loading) return;
    const userText = this.inputMsg;
    this.messages.push({ role: 'user', content: userText });
    this.inputMsg = '';
    this.loading = true;
    
    this.scrollToBottom();  

    try {
        // 使用你项目自带的 request 工具
        const res = await request.post("http://localhost:9999/api/ai/ask", { query: userText });
        
        // 这里的判断逻辑根据 AIChatController 返回的结构（Dify 返回 answer 字段）
        if (res.data && res.data.answer) {
            this.messages.push({ role: 'bot', content: res.data.answer });
        } else {
            this.$message.error("助手响应异常");
        }
          } catch (error) {
              console.error("AI请求失败", error);
              this.$message.error("网络连接失败，请检查后端接口");
          } finally {
              this.loading = false;
              this.scrollToBottom();
          }
      },
      scrollToBottom() {
          this.$nextTick(() => {
              const box = this.$refs.messageBox;
              if (box) box.scrollTop = box.scrollHeight;
          });
      }
    },
    created() {
        this.requestPeople();
        this.requestBed();
    },
    
};
</script>
<style lang="scss" scoped>
title{
  cursor: pointer;
}
.el-header {
  background-color: #427cb3;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .words {
    text-align: center;
    span {
      color: black;
    }
  }

  //border-bottom: 1px solid lightgrey;
}
.el-container{
  height: 100%;
}
.el-aside{
  background-color:#353744;
  border-right: 1px solid lightgrey;
}
.el-menu{
  border: 0;
}
.head-bar {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 70px;
  font-size: 22px;
  color: #fff;

}

.header-ico {
  float: left;
  padding: 0 21px;
  line-height: 70px;
}

.head-bar .logo {
  float: left;
  width: 250px;
  line-height: 70px;
  margin-left: -25px;
}

.head-right {
  float: right;
  padding-right: 50px;
}

.head-user-con {
  display: flex;
  height: 70px;
  align-items: center;
}

.btn-fullscreen {
  transform: rotate(45deg);
  margin-right: 5px;
  font-size: 24px;
}

.btn-fullscreen {
  position: relative;
  width: 30px;
  height: 30px;
  text-align: center;
  border-radius: 15px;
  cursor: pointer;
}

.btn-bell .el-icon-bell {
  color: #fff;
}

.user-name {
  margin-left: 10px;
}

.user-avatar {
  margin-left: 20px;
}

.user-avatar img {
  display: block;
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.el-dropdown-link {
  color: #fff;
  cursor: pointer;
}

.el-dropdown-menu__item {
  text-align: center;
}
.userFont {
    height: 150px;
    width: 250px;
    float: right;
    color: white;
    .spanCure {
        font-size: 15px;
        margin-top: 60px;
        margin-bottom: 15px;
    }
    .spanPeople {
        font-size: 18px;
    }
}

.userImage {
    height: 150px;
    width: 150px;
    font-size: 130px;
    color: white;
    position: relative;
    left: 40px;
    top: 10px;
    float: left;
}
.indexPeople {
    height: 200px;
    width: 440px;
    background: #58b9ae;
    float: left;
    margin: 30px;
}
.ai-assistant {
    position: fixed;
    right: 20px;
    bottom: 20px;
    z-index: 2000;

    .ai-btn {
        width: 60px;
        height: 60px;
        font-size: 24px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    }

    .chat-window {
        width: 350px;
        height: 500px;
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.15);
        display: flex;
        flex-direction: column;
        overflow: hidden;

        .chat-header {
            background: #427cb3;
            color: #fff;
            padding: 15px;
            display: flex;
            justify-content: space-between;
            font-weight: bold;
        }

        .chat-body {
            flex: 1;
            padding: 15px;
            overflow-y: auto;
            background: #f4f7f9;

            .msg-item {
                margin-bottom: 12px;
                display: flex;
                &.user { flex-direction: row-reverse; }
                
                .msg-content {
                    max-width: 80%;
                    padding: 8px 12px;
                    border-radius: 6px;
                    font-size: 14px;
                    line-height: 1.5;
                }
                &.bot .msg-content { background: #fff; border: 1px solid #ddd; }
                &.user .msg-content { background: #427cb3; color: #fff; }
            }
        }

        .chat-footer {
            padding: 10px;
            border-top: 1px solid #eee;
        }
    }
}
</style>